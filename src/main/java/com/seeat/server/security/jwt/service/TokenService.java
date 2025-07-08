package com.seeat.server.security.jwt.service;

import com.seeat.server.global.service.RedisService;
import com.seeat.server.global.util.JwtConstants;
import com.seeat.server.security.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
/**
 * 토큰 발급 서비스
 *
 * 액세스 토큰과 리프레시 토큰을 생성,
 * 리프레시 토큰은 Redis에 저장,
 * 액세스 토큰은 헤더, 리프레시 토큰은 쿠키로 전달
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    public void generateTokensAndSetHeaders(Authentication authentication, HttpServletResponse response, Long userId) {
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        redisService.setRefreshToken(userId, refreshToken, Duration.ofMillis(refreshTokenExpiration));

        response.setHeader(HttpHeaders.AUTHORIZATION, JwtConstants.TOKEN_TYPE + " " + accessToken);

        Cookie refreshTokenCookie = new Cookie(JwtConstants.REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(sslEnabled);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));

        response.addCookie(refreshTokenCookie);
    }
}
