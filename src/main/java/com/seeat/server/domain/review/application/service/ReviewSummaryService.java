package com.seeat.server.domain.review.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.domain.review.application.dto.response.ReviewSummaryResponse;
import com.seeat.server.domain.review.application.usecase.ReviewSummaryUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.external.LangchainApi;
import com.seeat.server.domain.review.external.LangchainApiException;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.seeat.server.global.util.RedisKeyUtil.REVIEW_SUMMARY_KEY;

/**
 * 리뷰 요약 서비스
 * - AI를 바탕으로 상영관에 존재하는 후기들을 요약한 정보를 가져오도록 합니다.
 * - 서버에서는 요청보내고 온 내용은 redis 저장하고,
 * - ttl이 끝나고 다시 요청하면 그때는 vectorRag에서 요청한다.
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewSummaryService implements ReviewSummaryUseCase {

    private final LangchainApi langchainApi;

    /// 외부 의존성
    private final TheaterUseCase theaterService;
    private final ReviewUseCase reviewService;
    private final ObjectMapper objectMapper;

    /**
     * 상영관 ID를 바탕으로 랭체인 요청 보내는 함수
     * 레디스에서 값 가져오기, 없으면 동기화로 AI에게 요청
     * @param auditoriumId  상영관 ID
     */
    @Override
    @Cacheable(cacheNames = REVIEW_SUMMARY_KEY, key = "#auditoriumId")
    public ReviewSummaryResponse loadSummaryByAuditoriumId(String auditoriumId) {

        /// 상영관 예외처리
        Auditorium auditorium = theaterService.getAuditorium(auditoriumId);

        /// 이름 정의
        String auditoriumName = auditorium.getTheater().getName() + " " + auditorium.getName();

        /// 요약을 진행할 리뷰가 존재하는지 체크
        Long hasReviews = reviewService.countsReviewsByAuditoriumId(auditorium.getId());

        /// 요약을 할 리뷰가 10개 이하면 불가능
        if (hasReviews < 10L) {
            return ReviewSummaryResponse.from(auditorium.getId(), auditoriumName, "10개 이상의 리뷰가 존재할 때부터 요약이 가능합니다.");
        }

        /// 상영관 아이디 전송으로 AI에게 요약 정보 요청하기
        String summary;

        try {
            String rawSummary = langchainApi.postSummaryByLangchain(auditorium.getId())
                    .block();

            /// rawSummary는 문자열 형태의 JSON이므로 파싱

            JsonNode rootNode = objectMapper.readTree(rawSummary);

            /// "answer" 필드 추출
            summary = rootNode.get("answer").asText();

            /// 개행 문자 제거
            summary = summary.replace("\n", "");

        } catch (LangchainApiException | JsonProcessingException e) {
            /// 실패의 경우 프런트에게 예외 메세지 처리
            throw new IllegalStateException(ErrorCode.INTERNAL_LANGCHAIN_ERROR.getMessage());
        }

        /// 결과 응답하기
        return ReviewSummaryResponse.from(auditorium.getId(), auditoriumName, summary);
    }
}
