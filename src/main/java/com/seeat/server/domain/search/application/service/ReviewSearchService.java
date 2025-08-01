package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewHashTagUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.search.application.dto.request.ReviewSearchCondition;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.usecase.ReviewSearchUseCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSearch;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import com.seeat.server.global.service.RecentSearchRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewSearchService implements ReviewSearchUseCase {

    private final SearchRepository repository;

    // 외부
    private final UserUseCase userService;
    private final UserSearchRepository userSearchRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewHashTagUseCase reviewHashTagService;
    private final RecentSearchRedisService recentSearchRedisService;

    /**
     * 인기순, 평점순, 최신순 기준으로 리뷰 검색(조회)
     *
     * @param condition 필터 요청 DTD
     * @param userId 유저 Id
     * @param pageRequest 페이징
     * @return SliceResponse<ReviewSearchResponse>
     */
    public SliceResponse<ReviewSearchResponse> getReviewList(
            ReviewSearchCondition condition, String guestToken,
            Long userId, PageRequest pageRequest){

        User user = null;

        if (userId != null) {
            // 유저 예외
            user = userService.getUser(userId);

            // 유저 검색어 저장하기 (저장되어있으면 넘어가기)
            Search search = repository.findByContent(condition.getKeyword())
                    .orElseGet(() -> repository.save(Search.of(condition.getKeyword())));

            // 유저 검색 엔티티 조회 후 없으면 저장
            boolean exists = userSearchRepository.existsByUserAndSearch(user, search);
            if (!exists) {
                UserSearch userSearch = UserSearch.of(user, search);
                userSearchRepository.save(userSearch);
            }
        } else if (guestToken != null) { // 비회원 token 저장
            recentSearchRedisService.addRecentSearch(guestToken, condition.getKeyword());
        } else {
            // 비회원인데 토큰도 null 이면 에러 처리
            throw new IllegalArgumentException(ErrorCode.INVALID_TOKEN.getMessage());
        }

        // 페이징 처리
        org.springframework.data.domain.PageRequest pageable = PageUtil.getPageable(pageRequest);

        // 커스텀 레포로 조회
        Slice<Review> reviews = reviewRepository.searchReviewsWithFilters(condition, pageable);

        // 조회
        List<Review> reviewList = reviews.getContent();
        List<List<String>> hashTags = reviewHashTagService.getHashTagsForReviews(reviewList);
        List<ReviewLike> userReviewLikes = Collections.emptyList();

        // 비회원 좋아요 누른 것 false 처리
        if (user != null) {
            userReviewLikes = reviewLikeRepository.findAllByUserAndReviewIn(user, reviewList);
        }

        // 좋아요 수 조회
        List<Object[]> likeCounts = reviewLikeRepository.countLikesByReviewIn(reviewList);
        Map<Long, Long> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        obj -> (Long) obj[0],
                        obj -> (Long) obj[1]
                ));

        // DTO 변환
        List<ReviewSearchResponse> responses = ReviewSearchResponse.from(reviewList, userReviewLikes, likeCountMap, hashTags);

        Slice<ReviewSearchResponse> slice = new SliceImpl<>(responses, pageable, reviews.hasNext());

        return SliceResponse.from(slice);
    }
}
