package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.request.BookmarkRequest;
import com.seeat.server.domain.review.application.dto.response.ReviewListResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "북마크 API", description = "북마크를 설정/해제하는 API 입니다.")
public interface BookmarkControllerSpec {


    @Operation(
            description = "북마크 설정 API",
            summary = "북마크 설정 API 입니다."
    )
    ApiResponse<Void> saveBookmark(
            @RequestBody @Valid BookmarkRequest request);

    @Operation(
            description = "북마크 조회 API",
            summary = "북마크 조회 API 입니다."
    )
    ApiResponse<SliceResponse<ReviewListResponse>> getBookmarksByUser(
            @AuthenticationPrincipal User user,
            PageRequest pageRequest);

    @Operation(
            description = "북마크 삭제 API",
            summary = "북마크 삭제 API 입니다."
    )
    ApiResponse<Void> deleteBookmark(
            @AuthenticationPrincipal User user,
            @PathVariable Long bookmarkId);



}
