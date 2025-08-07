package com.seeat.server.domain.bookmark.presentation;

import com.seeat.server.domain.bookmark.application.usecase.BookmarkUseCase;
import com.seeat.server.domain.bookmark.presentation.swagger.BookmarkControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController implements BookmarkControllerSpec {

    private final BookmarkUseCase bookmarkService;

    /**
     * 북마크 생성
     *
     * @param user       유저
     * @param reviewId   북마크할 리뷰 ID
     */
    @PostMapping()
    public ApiResponse<Void> saveBookmark(@AuthenticationPrincipal CustomUserInfo user,
                                          @RequestParam Long reviewId) {
        /// 서비스 호출
        bookmarkService.createBookmark(reviewId, user.getId());

        /// 응답
        return ApiResponse.created();
    }

    /**
     * 북마크 삭제
     * @param user          유저
     * @param bookmarkId    삭제할 북마크 ID
     */
    @DeleteMapping("/{bookmarkId}")
    public ApiResponse<Void> deleteBookmark(
            @AuthenticationPrincipal CustomUserInfo user,
            @PathVariable Long bookmarkId
    ) {
        /// 서비스 호출
        bookmarkService.deleteBookmark(bookmarkId, user.getId());

        return ApiResponse.deleted();
    }

}
