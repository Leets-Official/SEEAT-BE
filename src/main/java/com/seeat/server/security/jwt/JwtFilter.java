package com.seeat.server.security.jwt;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.global.util.JwtConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
/**
 * JWT 인증 필터
 *
 * HTTP 요청의 헤더에서 액세스 토큰을 검사
 * 액세스 토큰이 없거나 만료된 경우, 쿠키에 있는 리프레시 토큰을 확인하고
 * Redis에 저장된 리프레시 토큰과 일치하면 새로운 액세스 토큰을 발급해
 * 응답 헤더에 추가함
 *
 * 그렇지 않으면 인증 실패 처리
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        String accessToken = resolveToken(request, HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            String refreshToken = resolveRefreshTokenFromCookie(request);

            if (StringUtils.hasText(refreshToken) && jwtProvider.validateToken(refreshToken)) {
                Authentication refreshAuth = jwtProvider.getAuthentication(refreshToken);

                User user = (User) refreshAuth.getPrincipal();
                Long userId = user.getId();

                String redisRefreshToken = redisService.getRefreshToken(userId);

                if (refreshToken.equals(redisRefreshToken)) {
                    Authentication authentication = refreshAuth;
                    String newAccessToken = jwtProvider.generateAccessToken(authentication);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    response.setHeader(HttpHeaders.AUTHORIZATION, JwtConstants.TOKEN_TYPE + " " + newAccessToken);

                } else {

                    throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, null);
                }
            }
        }
        try {

            filterChain.doFilter(request, response);

        } catch (ServletException | IOException e) {

            throw new RuntimeException(e);
        }
    }

    private String resolveToken(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_TYPE + " ")) {
            return bearerToken.substring(JwtConstants.TOKEN_TYPE.length() + 1);
        }
        return null;
    }

    private String resolveRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (JwtConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
