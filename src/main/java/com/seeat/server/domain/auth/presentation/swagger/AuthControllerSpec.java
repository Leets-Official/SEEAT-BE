package com.seeat.server.domain.auth.presentation.swagger;

import com.seeat.server.domain.auth.application.dto.response.TokenResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "토큰 조회 API", description = "토큰 조회하는 API 입니다.")
public interface AuthControllerSpec {
    @Operation(summary = "AccessToken 조회", description = "RefreshToken 기반 AccessToken 재발급")
    @GetMapping
    ApiResponse<TokenResponse> getAccessToken(HttpServletRequest request);

}
