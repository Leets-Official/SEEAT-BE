package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.usecase.BookmarkUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkUseCase bookmarkService;

    @PostMapping
    public ApiResponse<Void> saveBookmark(@RequestBody @Valid BookmarkRequest request) {

        /// 서비스 호출
        bookmarkService.createBookmark(request);

        /// 응답
        return ApiResponse.created();
    }

    @GetMapping()
    public ApiResponse<SliceResponse<ReviewListResponse>> getBookmarksByUser(
            @AuthenticationPrincipal User user,
            PageRequest pageRequest) {

        /// 서비스 호출
        Slice<ReviewListResponse> responses = bookmarkService.loadMyBookmarks(user.getId(), pageRequest);

        /// DTO 변환
        SliceResponse<ReviewListResponse> response = SliceResponse.from(responses);

        return ApiResponse.ok(response);
    }

    @DeleteMapping("/{bookmarkId}")
    public ApiResponse<Void> deleteBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookmarkId
    ) {
        /// 서비스 호출
        bookmarkService.deleteBookmark(bookmarkId, user.getId());

        return ApiResponse.deleted();
    }

}
