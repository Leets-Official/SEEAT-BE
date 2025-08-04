package com.seeat.server.security.handler;

import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
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

        String origin = request.getHeader("Origin");
        String frontUrl = frontDevUrl;

        if (origin != null) {
            if (frontLocalUrl.equals(origin)) {
                frontUrl = frontLocalUrl;
            } else if (frontDevUrl.equals(origin)) {
                frontUrl = frontDevUrl;
            }
        }

        try {
            switch (userInfo.getStatus()) {
                case EXISTING_USER -> {
                    tokenService.generateTokensAndSetHeaders(response, userInfo.getUser());
                    response.sendRedirect(frontUrl + "/home");  // changed here
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

                    response.sendRedirect(extraInfoUrl);  // changed here
                }
                case EMAIL_DUPLICATE -> {
                    response.sendRedirect(frontUrl + "/login/duplicate-email"); // changed here
                }
                default -> {
                    throw new CustomException(ErrorCode.OAUTH2_UNKNOWN_STATUS, null);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
