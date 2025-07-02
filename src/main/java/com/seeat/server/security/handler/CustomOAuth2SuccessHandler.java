package com.seeat.server.security.handler;

import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException{

        CustomUserInfo userInfo = (CustomUserInfo) authentication.getPrincipal();

        switch (userInfo.getStatus()) {
            case EXISTING_USER -> {
                // JWT, 세션, 쿠키 추가
                redirectStrategy.sendRedirect(request, response, "/");
            }
            case NEW_USER -> {
                String tempUserKey = "OAUTH2_TEMP_USER:" + UUID.randomUUID();

                TempUserInfo tempUserInfo = new TempUserInfo(
                        userInfo.getTempUserInfo().getEmail(),
                        userInfo.getTempUserInfo().getProviderId(),
                        userInfo.getSocial(),
                        userInfo.getTempUserInfo().getNickname()
                );

                redisTemplate.opsForValue().set(tempUserKey, tempUserInfo, Duration.ofMinutes(10));
                request.getSession().setAttribute("OAUTH2_TEMP_USER_KEY", tempUserKey);

                redirectStrategy.sendRedirect(request, response, "/signup/extra-info");
            }
            case EMAIL_DUPLICATE -> {
                // 공통 에러처리로
                redirectStrategy.sendRedirect(request, response, "/login/duplicate-email");
            }
            default -> {
                // 공통 에러처리로
                redirectStrategy.sendRedirect(request, response, "/login");
            }
        }
    }

}
