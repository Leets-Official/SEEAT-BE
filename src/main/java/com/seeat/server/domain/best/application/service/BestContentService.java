package com.seeat.server.domain.best.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.best.application.usecase.BestContentUseCase;
import com.seeat.server.domain.best.application.util.SnapShotConverter;
import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import com.seeat.server.domain.best.domain.repository.BestAuditoriumRepository;
import com.seeat.server.domain.best.domain.repository.BestReviewRepository;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.theater.application.TheaterUseCase;
import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.seeat.server.global.util.RedisKeyUtil.*;

/**
 * [기존 서비스들을 활용하여 인기가 있는 리뷰/상영관을 조회하는 서비스]
 * - 6시간마다 스케쥴링으로 작동
 * - 레디스 캐싱 기능 구현
 * - 인기 상영관 목록 조회
 * - 인기 리뷰 목록 조회
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BestContentService implements BestContentUseCase {

    private final BestReviewRepository reviewRepository;
    private final BestAuditoriumRepository auditoriumRepository;

    /// 레디스 저장 의존성
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final Duration REDIS_TTL = Duration.ofHours(1);

    /// 외부 의존성
    private final ReviewUseCase reviewService;
    private final TheaterUseCase theaterService;

    /**
     * 베스트 후기 목록 조회
     * @param pageRequest   페이지
     */
    @Override

    public SliceResponse<BestReviewListResponse> loadBestReviews(PageRequest pageRequest) {

        /// PageRequest로부터 Spring Data의 Pageable 객체 생성
        Pageable pageable = PageUtil.getPageable(pageRequest);

        /// Redis에서 캐시된 JSON 문자열을 가져옴 (없을 수도 있으므로 Optional로 감쌈)
        Optional<String> cachedJson = Optional.ofNullable(redisTemplate.opsForValue().get(BEST_REVIEW_LIST_KEY));

        /// Redis에 캐시가 존재한다면
        if (cachedJson.isPresent()) {
            try {
                /// JSON 문자열을 BestReviewSnapshot 리스트로 역직렬화
                List<BestReviewSnapshot> snapshots = objectMapper.readValue(
                        cachedJson.get(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BestReviewSnapshot.class)
                );

                log.info("[BestContent] 레디스에서 조회");

                /// 페이징 계산을 위한 값 추출
                int page = pageRequest.getPage() - 1;   // 요청된 페이지 번호
                int size = pageRequest.getSize();   // 요청된 페이지 크기

                /// 실제로 잘라낼 시작 인덱스와 끝 인덱스를 계산
                int start = page * size;
                int end = Math.min(start + size, snapshots.size());  // 범위를 벗어나지 않도록 제한

                /// 시작 인덱스가 리스트 크기보다 크면 빈 페이지 반환
                if (start >= snapshots.size()) {
                    return SliceResponse.from(new SliceImpl<>(List.of(), pageable, false));
                }

                /// 지정한 범위만큼 subList로 잘라냄 (page에 해당하는 데이터만 추출)
                List<BestReviewSnapshot> pageSnapshots = snapshots.subList(start, end);

                /// Snapshot → DTO로 변환
                List<BestReviewListResponse> responses = BestReviewListResponse.from(pageSnapshots);

                /// 다음 페이지가 있는지 여부 판단 (hasNext는 end 인덱스가 전체 리스트보다 작을 경우 true)
                boolean hasNext = end < snapshots.size();

                /// SliceImpl로 슬라이스 객체 생성 후 커스텀 응답 객체로 변환
                Slice<BestReviewListResponse> slice = new SliceImpl<>(responses, pageable, hasNext);
                return SliceResponse.from(slice);

            } catch (JsonProcessingException e) {
                /// Redis 역직렬화 실패 시 로그만 남기고 DB 조회로 fallback
                log.error("[BestContent] Redis 역직렬화 실패: {}", e.getMessage(), e);
            }
        }

        // Redis에 캐시가 없는 경우, 먼저 스냅샷 DB 조회
        log.info("[BestContent] DB에서 조회");

        // Redis 캐시 없거나 역직렬화 실패 시 스냅샷 테이블에서 페이징 조회
        Slice<BestReviewSnapshot> snapshots = reviewRepository.findAll(pageable);

        if (snapshots.isEmpty()) {
            // 스냅샷 DB에도 데이터가 없으면 기존 조인 쿼리로 조회!
            log.info("[BestContent] 기존 쿼리에서 조회");
            return reviewService.getBestReviews(pageRequest);
        }

        List<BestReviewListResponse> responses = BestReviewListResponse.from(snapshots.getContent());
        Slice<BestReviewListResponse> slice = new SliceImpl<>(responses, snapshots.getPageable(), snapshots.hasNext());
        return SliceResponse.from(slice);

    }


    /**
     * 베스트 상영관 목록 조회
     *
     * @param pageRequest 페이지
     */
    @Override
    public SliceResponse<BestAuditoriumListResponse> loadBestTheaters(PageRequest pageRequest) {

        /// 페이징
        Pageable pageable = PageUtil.getPageable(pageRequest);

        /// 레디스에서 있는지 가져오기
        Optional<String> cachedJson = Optional.ofNullable(redisTemplate.opsForValue().get(BEST_AUDITORIUM_LIST_KEY));

        /// 레디스에 존재한다면
        if (cachedJson.isPresent()) {
            try {
                List<BestAuditoriumSnapshot> snapshots = objectMapper.readValue(
                        cachedJson.get(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, BestAuditoriumSnapshot.class)
                );

                /// DTO 변경
                log.info("[BestContent] 레디스에서 조회");

                /// 페이징 계산을 위한 값 추출
                int page = pageRequest.getPage() - 1;   // 요청된 페이지 번호
                int size = pageRequest.getSize();   // 요청된 페이지 크기

                /// 실제로 잘라낼 시작 인덱스와 끝 인덱스를 계산
                int start = page * size;
                int end = Math.min(start + size, snapshots.size());  // 범위를 벗어나지 않도록 제한

                /// 시작 인덱스가 리스트 크기보다 크면 빈 페이지 반환
                if (start >= snapshots.size()) {
                    return SliceResponse.from(new SliceImpl<>(List.of(), pageable, false));
                }

                /// 지정한 범위만큼 subList로 잘라냄 (page에 해당하는 데이터만 추출)
                List<BestAuditoriumSnapshot> pageSnapshots = snapshots.subList(start, end);

                /// Snapshot → DTO로 변환
                List<BestAuditoriumListResponse> responses = BestAuditoriumListResponse.from(pageSnapshots);

                /// 다음 페이지가 있는지 여부 판단 (hasNext는 end 인덱스가 전체 리스트보다 작을 경우 true)
                boolean hasNext = end < snapshots.size();

                /// SliceImpl로 슬라이스 객체 생성 후 커스텀 응답 객체로 변환
                Slice<BestAuditoriumListResponse> slice = new SliceImpl<>(responses, pageable, hasNext);
                return SliceResponse.from(slice);

            } catch (JsonProcessingException e) {
                /// 역직렬화 실패시 아래 DB 조회로 넘어가기에 로그만 찍어둔다.
                log.error("[BestContent] Redis 역직렬화 실패: {}", e.getMessage(), e);
            }
        }

        // Redis 캐시 없거나 역직렬화 실패 시 스냅샷 테이블에서 페이징 조회
        Slice<BestAuditoriumSnapshot> snapshots = auditoriumRepository.findAll(pageable);

        /// DB에서 조회
        log.info("[BestContent] DB에서 조회");

        if (snapshots.isEmpty()) {
            // 스냅샷 DB에도 데이터가 없으면 기존 조인 쿼리로 조회!
            log.info("[BestContent] 기존 쿼리에서 조회");
            return theaterService.loadBestAuditoriums(pageRequest);
        }

        List<BestAuditoriumListResponse> responses = BestAuditoriumListResponse.from(snapshots.getContent());
        Slice<BestAuditoriumListResponse> slice = new SliceImpl<>(responses, snapshots.getPageable(), snapshots.hasNext());
        return SliceResponse.from(slice);
    }



    /**
     * TODO 스프링 배치를 통한 레디스 스케쥴링
     * TODO 레디스에 넣어두고, 스냅샷을 통해 TTL 이 없을 경우 대처하기 위해 DB에서 추가 구현
     * TODO! 6시간 간격마다 스케쥴링을 돌리거나 직접 버튼을 눌러 코드로 구현
     */
    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void saveBestContents() {

        /// 페이지 요청
        PageRequest pageRequest = PageRequest.builder()
                .page(1)
                .size(20)
                .build();

        /// 리뷰 저장
        saveBestReviews(pageRequest);

        /// 상영관 저장
        saveBestAuditoriums(pageRequest);

    }


    /**
     * 인기 리뷰에서 삭제가 발생했을 때, 전체적으로 리뷰를 최신화하는 작업이 필요
     */
    @Override
    public void deleteBestContents() {


    }

    /**
     * 후기 저장을 위한 함수
     * @param pageRequest   페이징
     */
    private void saveBestReviews(PageRequest pageRequest) {

        /// 기존 값 삭제 후 새로 저장
        reviewRepository.deleteAll();

        /// 베스트 후기 조회 후, 저장하기
        SliceResponse<BestReviewListResponse> response = reviewService.getBestReviews(pageRequest);

        /// 베스트 후기 DB에 저장하기
        List<BestReviewSnapshot> reviewSnapshot = SnapShotConverter.toReviewSnapshot(response.content());

        /// 값이 없다면 저장하지 않는다.
        if (reviewSnapshot == null || reviewSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - Redis 저장 생략");
            return;
        }

        /// DB 저장
        reviewRepository.saveAll(reviewSnapshot);

        /// 레디스에 저장하기
        try {
            String reviewJson = objectMapper.writeValueAsString(reviewSnapshot);
            redisTemplate.opsForValue()
                    .set(BEST_REVIEW_LIST_KEY, reviewJson, REDIS_TTL);
        } catch (JsonProcessingException e) {
            log.error("[BestContent] Redis 직렬화 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 상영관 저장을 위한 함수
     * @param pageRequest   페이징
     */
    private void saveBestAuditoriums(PageRequest pageRequest) {

        /// 기존 값 삭제 후 새로 저장
        auditoriumRepository.deleteAll();

        /// 인기 상영관 조회
        SliceResponse<BestAuditoriumListResponse> response = theaterService.loadBestAuditoriums(pageRequest);

        /// 스냅샷 변환
        List<BestAuditoriumSnapshot> auditoriumSnapshot = SnapShotConverter.toAuditoriumSnapshot(response.content());

        if (auditoriumSnapshot == null || auditoriumSnapshot.isEmpty()) {
            log.info("[BestContent] 스냅샷 변환 결과 없음 - Redis 저장 생략");
            return;
        }

        /// DB 저장
        auditoriumRepository.saveAll(auditoriumSnapshot);

        /// Redis 저장
        try {
            String auditoriumJson = objectMapper.writeValueAsString(auditoriumSnapshot);
            redisTemplate.opsForValue().set(BEST_AUDITORIUM_LIST_KEY, auditoriumJson, REDIS_TTL);
        } catch (JsonProcessingException e) {
            log.error("[BestContent] Redis 직렬화 실패: {}", e.getMessage(), e);
        }
    }


}
