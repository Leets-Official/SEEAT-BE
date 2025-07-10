package com.seeat.server.security.handler;

import com.seeat.server.global.util.RedisKeyUtil;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${cors.front.local}")
    private String frontLocalUrl;
    @Value("${cors.front.dev}")
    private String frontDevUrl;
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    private final TokenService tokenService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        CustomUserInfo userInfo = (CustomUserInfo) authentication.getPrincipal();
        String frontUrl = getFrontUrl();
        try {
            switch (userInfo.getStatus()) {
                case EXISTING_USER -> {
                    tokenService.generateTokensAndSetHeaders(authentication, response, userInfo.getId());
                    redirectStrategy.sendRedirect(request, response, frontUrl + "/");
                }
                case NEW_USER -> {
                    String tempUserKey = RedisKeyUtil.generateOAuth2TempUserKey();

                    TempUserInfo tempUserInfo = new TempUserInfo(
                            userInfo.getTempUserInfo().getEmail(),
                            userInfo.getTempUserInfo().getProviderId(),
                            userInfo.getSocial(),
                            userInfo.getTempUserInfo().getNickname()
                    );

                    redisTemplate.opsForValue().set(tempUserKey, tempUserInfo, Duration.ofMinutes(10));

                    String extraInfoUrl = frontUrl + "/extra-info?tempKey=" + tempUserKey;

                    redirectStrategy.sendRedirect(request, response, extraInfoUrl);
                }
                case EMAIL_DUPLICATE -> {

                    redirectStrategy.sendRedirect(request, response, frontUrl + "/login/duplicate-email");
                }
                default -> {

                    redirectStrategy.sendRedirect(request, response, frontUrl + "/login");
                }
            }
        } catch (IOException e){

            throw new RuntimeException(e);
        }
    }

    private String getFrontUrl() {
        return "dev".equals(activeProfile) ? frontDevUrl : frontLocalUrl;
    }

}
