package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.review.application.usecase.BookmarkUseCase;
import com.seeat.server.domain.review.presentation.swagger.BookmarkControllerSpec;
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
public class BookmarkController implements BookmarkControllerSpec {

    private final BookmarkUseCase bookmarkService;

    /**
     * 북마크 생성
     * @param request   요청DTO
     */
    @PostMapping
    public ApiResponse<Void> saveBookmark(@RequestBody @Valid BookmarkRequest request) {

        /// 서비스 호출
        bookmarkService.createBookmark(request);

        /// 응답
        return ApiResponse.created();
    }

    /**
     * 북마크 삭제
     * @param user          삭제할 유저
     * @param bookmarkId    삭제할 ID
     */
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
