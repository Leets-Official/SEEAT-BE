package com.seeat.server.security.handler;

import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.util.RedisKeyUtil;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomOAuth2SuccessHandlerTest {
    @InjectMocks
    private CustomOAuth2SuccessHandler successHandler;

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedirectStrategy redirectStrategy;

    @BeforeEach
    void setUp() {
        successHandler = new CustomOAuth2SuccessHandler(redisTemplate, tokenService);
        ReflectionTestUtils.setField(successHandler, "redirectStrategy", redirectStrategy);
    }

    @Test
    void existingUser_cookieSet_redirect() throws Exception {
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
        given(principal.getId()).willReturn(1L);

        // 프로퍼티 설정
        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(successHandler, "activeProfile", "local");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(tokenService).generateTokensAndSetHeaders(authentication, response, 1L);
        verify(redirectStrategy).sendRedirect(request, response, "http://localhost:3000/");
    }

    @Test
    void newUser_extraInfo_redirect() throws Exception{
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        OAuth2UserInfo oauth2UserInfo = mock(OAuth2UserInfo.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        HttpSession session = mock(HttpSession.class);
        String mockTempKey = "OAUTH2_TEMP_USER:test-key-123";

        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.NEW_USER);
        given(principal.getTempUserInfo()).willReturn(oauth2UserInfo);
        given(oauth2UserInfo.getEmail()).willReturn("test@test.com");
        given(oauth2UserInfo.getProviderId()).willReturn("test1234");
        given(principal.getSocial()).willReturn(UserSocial.KAKAO);
        given(oauth2UserInfo.getNickname()).willReturn("nickname");

        given(redisTemplate.opsForValue()).willReturn(valueOperations);


        // 프로퍼티 설정
        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(successHandler, "activeProfile", "local");

        try (MockedStatic<RedisKeyUtil> redisKeyUtilMock = mockStatic(RedisKeyUtil.class)) {
            redisKeyUtilMock.when(RedisKeyUtil::generateOAuth2TempUserKey).thenReturn(mockTempKey);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            verify(valueOperations).set(eq(mockTempKey), any(TempUserInfo.class), eq(Duration.ofMinutes(10)));
            verify(redirectStrategy).sendRedirect(request, response, "http://localhost:3000/extra-info?tempKey=" + mockTempKey);
        }
    }

    @Test
    void emailDuplicate_error_redirect() throws Exception{
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EMAIL_DUPLICATE);

        // 프로퍼티 설정
        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://localhost:3001");
        ReflectionTestUtils.setField(successHandler, "activeProfile", "local");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(redirectStrategy).sendRedirect(request, response, "http://localhost:3000/login/duplicate-email");
    }

    @Test
    void dev_profile_uses_dev_url() throws Exception {
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
        given(principal.getId()).willReturn(1L);

        // 프로퍼티 설정 (dev 환경)
        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://dev.example.com");
        ReflectionTestUtils.setField(successHandler, "activeProfile", "dev");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(tokenService).generateTokensAndSetHeaders(authentication, response, 1L);
        verify(redirectStrategy).sendRedirect(request, response, "http://dev.example.com/");
    }
}
