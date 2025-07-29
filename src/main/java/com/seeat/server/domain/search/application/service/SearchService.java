package com.seeat.server.domain.search.application.service;

import com.seeat.server.domain.review.application.usecase.HashTagUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewLike;
import com.seeat.server.domain.review.domain.repository.ReviewLikeRepository;
import com.seeat.server.domain.review.domain.repository.ReviewRepository;
import com.seeat.server.domain.search.application.dto.response.ReviewSearchResponse;
import com.seeat.server.domain.search.application.dto.response.SearchResponse;
import com.seeat.server.domain.search.application.usecase.SearchUseCase;
import com.seeat.server.domain.search.domain.entity.Search;
import com.seeat.server.domain.search.domain.entity.SortType;
import com.seeat.server.domain.search.domain.repository.SearchRepository;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSearch;
import com.seeat.server.domain.user.domain.repository.UserSearchRepository;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.PageUtil;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchService implements SearchUseCase {

    private final SearchRepository repository;

    // 외부 의존성
    private final UserUseCase userService;
    private final UserSearchRepository userSearchRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final HashTagUseCase hashTagService;

    /**
     * 최근 검색 리스트 조회
     * @param userId 유저 Id
     * @return 최근 검색 리스트 DTO
     */
    @Override
    public List<SearchResponse> getSearchList(Long userId){

        // 유저 예외
        userService.getUser(userId);

        // searchList 조회
        List<Search> searches = userSearchRepository.findSearchListByUserId(userId);

        return SearchResponse.from(searches);
    }

    /**
     * 최근 검색어 삭제
     * @param searchId 검색 Id
     * @param userId 유저 Id
     */
    @Override
    public void deleteSearch(Long searchId, Long userId){

        // 유저 예외
        User user = userService.getUser(userId);

        // Search 예외
        Search search = getSearch(searchId);

        // 삭제 (userSearch 지운 후 searchId로 지움)
        userSearchRepository.deleteByUserAndSearch(user, search);
        repository.deleteById(searchId);
    }

    /**
     * 인기순, 평점순, 최신순 기준으로 리뷰 검색(조회)
     *
     * @param keyword 검색어
     * @param sort 정렬기준 (ex. 인기순, 평점순, 최신순)
     * @param userId 유저 Id
     * @param pageRequest 페이징
     * @return SliceResponse<ReviewSearchResponse>
     */
    public SliceResponse<ReviewSearchResponse> getReviewList(
            String keyword, SortType sort,
            Long userId, PageRequest pageRequest){

        // 유저 예외
        User user = userService.getUser(userId);

        // 유저 검색어 저장하기 (저장되어있으면 넘어가기)
        Search search = repository.findByContent(keyword)
                .orElseGet(() -> repository.save(Search.of(keyword)));

        // 유저 검색 엔티티 조회 후 없으면 저장
        boolean exists = userSearchRepository.existsByUserAndSearch(user, search);
        if (!exists) {
            UserSearch userSearch = UserSearch.of(user, search);
            userSearchRepository.save(userSearch);
        }

        // 페이징 처리
        org.springframework.data.domain.PageRequest pageable = PageUtil.getPageable(pageRequest);

        // 케이스 나누기 (인기순, 최신순, 평점순) 리뷰 조회
        // 필터 추가 해야함
        // 너무 길어서 분리하면 좋을 듯
        Slice<Review> reviews;

        switch (sort) {
            case POPULAR:
                reviews = reviewRepository.findPopularReviews(keyword, pageable);
                break;
            case RATING:
                reviews = reviewRepository.findReviewsOrderByRating(keyword, pageable);
                break;
            case LATEST:
                reviews = reviewRepository.findReviewsOrderByCreatedAt(keyword, pageable);
                break;
            default:
                throw new IllegalArgumentException(ErrorCode.INVALID_SORT_TYPE.getMessage());
        }

        // 조회
        List<Review> reviewList = reviews.getContent();
        List<Long> reviewIds = reviewList.stream()
                .map(Review::getId)
                .toList();
        List<ReviewLike> reviewLikes = reviewLikeRepository.findAllByUserAndReviewIn(user, reviewList);
        List<List<String>> hashTags = hashTagService.getHashTagsForReviews(reviewList);

        // DTO 변환
        List<ReviewSearchResponse> responses = ReviewSearchResponse.from(reviewList, reviewLikes, hashTags);

        Slice<ReviewSearchResponse> slice = new SliceImpl<>(responses, pageable, reviews.hasNext());

        return SliceResponse.from(slice);
    }



    // 공통 함수
    /**
     * @param searchId 검색어 Id
     */
    @Override
    public Search getSearch(Long searchId){
        return repository.findById(searchId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_SEARCH.getMessage()));
    }
}
