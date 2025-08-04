package com.seeat.server.security.handler;

import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.global.util.RedisKeyUtil;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2SuccessHandler")
class CustomOAuth2SuccessHandlerTest {

    private CustomOAuth2SuccessHandler successHandler;

    @Mock
    private TokenService tokenService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        successHandler = new CustomOAuth2SuccessHandler(redisTemplate, tokenService);
        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://dev.example.com");
        ReflectionTestUtils.setField(successHandler, "activeProfile", "local");
    }

    @Nested
    @DisplayName("Origin이 local일 때")
    class LocalOrigin {

        @BeforeEach
        void setOrigin() {
            given(request.getHeader("Origin")).willReturn("http://localhost:3000");
        }

        @Test
        @DisplayName("기존 유저: 토큰 발급 + /home 리다이렉트")
        void existingUser_shouldRedirectToLocalHome() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            User user = mock(User.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
            given(principal.getUser()).willReturn(user);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(tokenService).generateTokensAndSetHeaders(response, user);
            verify(response).sendRedirect("http://localhost:3000/home");
        }

        @Test
        @DisplayName("새 유저: 임시유저 Redis 저장 + /extra-info 리다이렉트")
        void newUser_shouldStoreTempUserInRedis_andRedirectToExtraInfo() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            OAuth2UserInfo oauth2UserInfo = mock(OAuth2UserInfo.class);
            ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);

            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.NEW_USER);
            given(principal.getTempUserInfo()).willReturn(oauth2UserInfo);
            given(principal.getSocial()).willReturn(UserSocial.KAKAO);
            given(oauth2UserInfo.getEmail()).willReturn("test@example.com");
            given(oauth2UserInfo.getProviderId()).willReturn("12345");
            given(oauth2UserInfo.getNickname()).willReturn("nickname");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            String tempKey = "OAUTH2_TEMP_USER:test-key";

            try (MockedStatic<RedisKeyUtil> mockedStatic = mockStatic(RedisKeyUtil.class)) {
                mockedStatic.when(RedisKeyUtil::generateOAuth2TempUserKey).thenReturn(tempKey);

                successHandler.onAuthenticationSuccess(request, response, authentication);

                verify(valueOperations).set(eq(tempKey), any(TempUserInfo.class), eq(Duration.ofMinutes(10)));
                verify(response).sendRedirect("http://localhost:3000/extra-info?tempKey=" + tempKey);
            }
        }

        @Test
        @DisplayName("이메일 중복: /login/duplicate-email 리다이렉트")
        void emailDuplicate_shouldRedirectToDuplicateEmailPage() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EMAIL_DUPLICATE);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect("http://localhost:3000/login/duplicate-email");
        }
    }

    @Nested
    @DisplayName("Origin이 dev일 때")
    class DevOrigin {

        @BeforeEach
        void setOrigin() {
            given(request.getHeader("Origin")).willReturn("http://dev.example.com");
        }

        @Test
        @DisplayName("기존 유저: 토큰 발급 + /home 리다이렉트")
        void existingUser_shouldRedirectToDevHome() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            User user = mock(User.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
            given(principal.getUser()).willReturn(user);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(tokenService).generateTokensAndSetHeaders(response, user);
            verify(response).sendRedirect("http://dev.example.com/home");
        }

        @Test
        @DisplayName("새 유저: 임시유저 Redis 저장 + /extra-info 리다이렉트")
        void newUser_shouldStoreTempUserInRedis_andRedirectToExtraInfo() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            OAuth2UserInfo oauth2UserInfo = mock(OAuth2UserInfo.class);
            ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);

            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.NEW_USER);
            given(principal.getTempUserInfo()).willReturn(oauth2UserInfo);
            given(principal.getSocial()).willReturn(UserSocial.KAKAO);
            given(oauth2UserInfo.getEmail()).willReturn("test@example.com");
            given(oauth2UserInfo.getProviderId()).willReturn("12345");
            given(oauth2UserInfo.getNickname()).willReturn("nickname");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            String tempKey = "OAUTH2_TEMP_USER:test-key";
            try (MockedStatic<RedisKeyUtil> mockedStatic = mockStatic(RedisKeyUtil.class)) {
                mockedStatic.when(RedisKeyUtil::generateOAuth2TempUserKey).thenReturn(tempKey);

                successHandler.onAuthenticationSuccess(request, response, authentication);

                verify(valueOperations).set(eq(tempKey), any(TempUserInfo.class), eq(Duration.ofMinutes(10)));
                verify(response).sendRedirect("http://dev.example.com/extra-info?tempKey=" + tempKey);
            }
        }

        @Test
        @DisplayName("이메일 중복: /login/duplicate-email 리다이렉트")
        void emailDuplicate_shouldRedirectToDuplicateEmailPage() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EMAIL_DUPLICATE);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect("http://dev.example.com/login/duplicate-email");
        }
    }

    @Nested
    @DisplayName("Origin이 없는 경우")
    class NoOrigin {

        @BeforeEach
        void setOrigin() {
            given(request.getHeader("Origin")).willReturn(null);
        }

        @Test
        @DisplayName("기본적으로 dev로 리다이렉트")
        void shouldRedirectToDevHome() throws Exception {
            CustomUserInfo principal = mock(CustomUserInfo.class);
            User user = mock(User.class);
            given(authentication.getPrincipal()).willReturn(principal);
            given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
            given(principal.getUser()).willReturn(user);

            successHandler.onAuthenticationSuccess(request, response, authentication);

            verify(tokenService).generateTokensAndSetHeaders(response, user);
            verify(response).sendRedirect("http://dev.example.com/home");
        }
    }
}
