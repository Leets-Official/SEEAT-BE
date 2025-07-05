package com.seeat.server;

import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.security.handler.CustomOAuth2SuccessHandler;
import com.seeat.server.security.jwt.service.TokenService;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

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

    @Test
    void existingUser_cookieSet_redirect() throws Exception {
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
        given(principal.getId()).willReturn(1L);

        given(request.getContextPath()).willReturn("");
        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(tokenService).generateTokensAndSetHeaders(authentication, response, 1L);
        verify(response).sendRedirect("/");
        verify(response, never()).sendRedirect("/signup/extra-info");
        verify(response, never()).sendRedirect("/login/duplicate-email");
    }

    @Test
    void newUser_extraInfo_redirect() throws Exception{
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        OAuth2UserInfo oauth2UserInfo = mock(OAuth2UserInfo.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        HttpSession session = mock(HttpSession.class);

        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.NEW_USER);
        given(principal.getTempUserInfo()).willReturn(oauth2UserInfo);
        given(oauth2UserInfo.getEmail()).willReturn("test@test.com");
        given(oauth2UserInfo.getProviderId()).willReturn("test1234");
        given(principal.getSocial()).willReturn(UserSocial.KAKAO);
        given(oauth2UserInfo.getNickname()).willReturn("nickname");
        given(request.getContextPath()).willReturn("");
        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(request.getSession()).willReturn(session);


        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(valueOperations).set(startsWith("OAUTH2_TEMP_USER:"), any(TempUserInfo.class), eq(Duration.ofMinutes(10)));
        verify(session).setAttribute(eq("OAUTH2_TEMP_USER_KEY"), startsWith("OAUTH2_TEMP_USER:"));
        verify(response).sendRedirect("/signup/extra-info");
        verify(response, never()).sendRedirect("/");
        verify(response, never()).sendRedirect("/login/duplicate-email");
    }

    @Test
    void emailDuplicate_error_redirect() throws Exception{
        // Given
        CustomUserInfo principal = mock(CustomUserInfo.class);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EMAIL_DUPLICATE);
        given(request.getContextPath()).willReturn("");
        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(response).sendRedirect("/login/duplicate-email");
        verify(response, never()).sendRedirect("/");
        verify(response, never()).sendRedirect("/signup/extra-info");

    }
}
