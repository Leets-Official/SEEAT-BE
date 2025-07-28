package com.seeat.server.domain.review.application.usecase;


import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.domain.entity.Bookmark;
import com.seeat.server.global.response.pageable.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * [북마크를 정의하는 인터페이스] 입니다
 */
public interface BookmarkUseCase {

    /// 북마크 저장
    Bookmark createBookmark(BookmarkRequest request);

    /// 나의 북마크 목록 조회
    Slice<ReviewListResponse> loadMyBookmarks(Long userId, PageRequest pageRequest);

    /// 북마크 해제
    void deleteBookmark(Long id, Long userId);

}
