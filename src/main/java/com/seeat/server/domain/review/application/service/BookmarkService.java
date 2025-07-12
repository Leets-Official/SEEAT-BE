package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.usecase.BookmarkUseCase;
import com.seeat.server.domain.review.application.usecase.ReviewUseCase;
import com.seeat.server.domain.review.domain.entity.Bookmark;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.repository.BookmarkRepository;
import com.seeat.server.domain.user.application.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.pageable.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seeat.server.global.response.pageable.PageUtil.getPageable;

@Service
@Transactional
@RequiredArgsConstructor
/**
 * 리뷰에 대한 북마크를 진행하는 서비스 입니다.
 */
public class BookmarkService implements BookmarkUseCase {

    private final BookmarkRepository repository;

    /// 외부 의존성 처리
    private final UserUseCase userService;
    private final ReviewUseCase reviewService;

    /**
     * 북마크를 생성하는 함수
     * @param request   생성 DTO
     */
    @Override
    public void createBookmark(BookmarkRequest request) {

        /// 유저 예외 처리
        User user = userService.getUser(request.getUserId());

        /// 리뷰 예외 처리
        Review review = reviewService.getReview(request.getReviewId());

        /// 동일한 북마크 처리 예외처리
        boolean checked = repository.existsByUserAndReview(user, review);

        if (checked) {
            throw new IllegalStateException(ErrorCode.DUPLICATE_BOOKMARK.getMessage());
        }

        /// 북마크 생성
        Bookmark bookmark = Bookmark.of(user, review);

        /// DB에 저장
        repository.save(bookmark);
    }


    /**
     * 유저 ID 바탕으로 나의 북마크 목록 조회
     * @param userId    유저 ID
     */
    @Override
    public Slice<ReviewListResponse> loadMyBookmarks(Long userId, PageRequest pageRequest) {

        /// 유저 예외처리
        User user = userService.getUser(userId);

        /// Pageable
        Pageable pageable = getPageable(pageRequest);

        /// DB 에서 조회
        Slice<Bookmark> bookmarks = repository.findByUser(user, pageable);

        /// 리뷰 목록 조회 후, DTO 변환
        List<Review> reviews = bookmarks.stream()
                .map(Bookmark::getReview)
                .toList();

        /// Slice 객체로 생성
        SliceImpl<Review> reviewSlice = new SliceImpl<>(reviews, bookmarks.getPageable(), bookmarks.hasNext());

        return reviewService.loadReviewsForBookmark(reviewSlice);
    }


    /**
     * 유저가 북마크를 삭제할 수 있도록 하는 함수 입니다.
     * @param id        삭제할 북마크 아이디
     * @param userId    삭제하는 유저 Id
     */
    @Override
    public void deleteBookmark(Long id, Long userId) {

        /// 유저 예외 처리
        User user = userService.getUser(userId);

        /// 유저에 해당 북마크가 존재하는지 확인
        boolean checked = repository.existsByIdAndUser(id, user);
        if (!checked) {
            throw new IllegalStateException(ErrorCode.NOT_OWN_BOOKMARK.getMessage());
        }

        /// DB 내 삭제
        repository.deleteById(id);
    }

}
