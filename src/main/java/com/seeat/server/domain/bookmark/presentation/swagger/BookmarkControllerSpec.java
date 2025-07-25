package com.seeat.server.domain.bookmark.presentation.swagger;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "북마크 API", description = "북마크를 설정/해제하는 API 입니다.")
public interface BookmarkControllerSpec {


    /**
     * 북마크 설정 API
     * @param user      유저
     * @param reviewId  리뷰ID
     */
    @Operation(
            description = "북마크 설정 API",
            summary = "북마크 설정 API 입니다."
    )
    ApiResponse<Void> saveBookmark(
            @AuthenticationPrincipal User user,
            @Parameter(description = "북마크할 리뷰ID", example = "1")
            @PathVariable Long reviewId);


    /**
     * 북마크 취소 API
     * @param user          유저
     * @param bookmarkId    취소할 북마크ID
     */
    @Operation(
            description = "북마크 삭제 API",
            summary = "북마크 삭제 API 입니다."
    )
    ApiResponse<Void> deleteBookmark(
            @AuthenticationPrincipal User user,
            @Parameter(description = "북마크 취소할 리뷰ID", example = "1")
            @PathVariable Long bookmarkId);



}
