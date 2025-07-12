package com.seeat.server.security.jwt;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.global.util.JwtConstants;
import com.seeat.server.security.config.RequestMatcherHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.security.Principal;

/**
 * JWT 인증 필터
 *
 * HTTP 요청의 헤더에서 액세스 토큰을 검사
 * 액세스 토큰이 없거나 만료된 경우, 쿠키에 있는 리프레시 토큰을 확인하고
 * Redis에 저장된 리프레시 토큰과 일치하면 새로운 액세스 토큰을 발급해
 * 응답 헤더에 추가함
 *
 * 그렇지 않으면 인증 실패 처리
 *
 * 0712 추가
 * - RequestMatcherHolder 도입해서, 필터를 도는 것 + 권한 설정을 한 곳에서 진행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final RequestMatcherHolder requestMatcherHolder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {


        /// 액세스 토큰 추출
        String accessToken = resolveToken(request, HttpHeaders.AUTHORIZATION);

        /// 검증 확인,인증 정보 체크후 시큐리티 홀더에 저장
        if (StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
            Authentication authentication = jwtProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            /// 리프레시 쿠키를 헤더에서 추출하여, 액세스 토큰 재발급
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

                    throw new JwtAuthenticationException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
                }

            }
        }
        try {

            filterChain.doFilter(request, response);

        } catch (ServletException | IOException e) {

            throw new RuntimeException(e);
        }
    }

    /**
     * 헤더에서 액세스 토큰을 가져오는 함수
     * @param request       서블릿
     * @param headerName    헤더 이름
     */
    private String resolveToken(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_TYPE + " ")) {
            return bearerToken.substring(JwtConstants.TOKEN_TYPE.length() + 1);
        }
        return null;
    }

    /**
     * 쿠키에서 리프레쉬 토큰을 가져오는 함수
     * @param request   서블릿
     */
    private String resolveRefreshTokenFromCookie(HttpServletRequest request) {

        /// 쿠키가 없다면 null
        if (request.getCookies() == null) {
            return null;
        }

        /// 쿠키가 존재한다면, 값 가져오기
        for (Cookie cookie : request.getCookies()) {
            if (JwtConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        /// requestMatcherHolder 통해 필터와 시큐리티를 한번에 해결, true 이면 필터 자체를 타지 않는다.
        /// ex) 개발용 토큰 자체에 필터를 타지 않도록 설정하여, 401,403 에러가 발생하지 않도록 설정
        boolean matches = requestMatcherHolder.getRequestMatchersByMinRole(null)
                .matches(request);

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("[로그] 주소 {}, 방식 {}, 결과 {}}", requestURI, method, matches);

        return matches;
    }
}
