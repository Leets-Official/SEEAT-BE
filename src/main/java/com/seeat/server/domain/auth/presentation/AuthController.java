package com.seeat.server.domain.auth.presentation;

import com.seeat.server.domain.auth.application.dto.response.TokenResponse;
import com.seeat.server.domain.auth.presentation.swagger.AuthControllerSpec;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.util.JwtConstants;
import com.seeat.server.security.jwt.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerSpec {

    private final TokenService tokenService;

    @GetMapping
    public ApiResponse<TokenResponse> getAccessToken(HttpServletRequest request){

        // 클라이언트 쿠키에서 refreshToken 추출
        String refreshToken = extractRefreshTokenFromCookies(request.getCookies());

        // 사용자 정보 조회
        Optional<User> user = tokenService.getUserFromRefreshToken(refreshToken);
        if (user.isEmpty()) {

            return ApiResponse.fail(new CustomException(ErrorCode.NOT_USER, null));
        }

        // 새로운 accessToken 생성
        String newAccessToken = tokenService.generateAccessToken(user.get());

        // accessToken 응답
        return ApiResponse.ok(new TokenResponse(newAccessToken));
    }

    private String extractRefreshTokenFromCookies(Cookie[] cookies) {
        // 쿠키에서 refreshToken 추출
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (JwtConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                // 쿠키 값 반환
                return cookie.getValue();
            }
        }
        // 없으면 null
        return null;
    }
}
