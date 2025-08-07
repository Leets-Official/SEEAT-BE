//package com.seeat.server.security.handler;
//
//import com.seeat.server.domain.user.domain.entity.User;
//import com.seeat.server.domain.user.domain.entity.UserSocial;
//import com.seeat.server.global.util.RedisKeyUtil;
//import com.seeat.server.security.jwt.service.TokenService;
//import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
//import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
//import com.seeat.server.security.oauth2.application.dto.response.OAuth2UserInfo;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.Duration;
//
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CustomOAuth2SuccessHandlerTest {
//
//    private CustomOAuth2SuccessHandler successHandler;
//
//    @Mock
//    private TokenService tokenService;
//
//    @Mock
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Mock
//    private HttpServletRequest request;
//
//    @Mock
//    private HttpServletResponse response;
//
//    @Mock
//    private Authentication authentication;
//
//    @BeforeEach
//    void setUp() {
//        successHandler = new CustomOAuth2SuccessHandler(redisTemplate, tokenService);
//
//        // @Value 주입 필드 설정
//        ReflectionTestUtils.setField(successHandler, "frontLocalUrl", "http://localhost:3000");
//        ReflectionTestUtils.setField(successHandler, "frontDevUrl", "http://dev.example.com");
//        ReflectionTestUtils.setField(successHandler, "activeProfile", "local");
//    }
//
//    @Test
//    void existingUser_shouldRedirectToHome_withTokenHeaders() throws Exception {
//        // Given
//        CustomUserInfo principal = mock(CustomUserInfo.class);
//        User user = mock(User.class);
//        given(authentication.getPrincipal()).willReturn(principal);
//        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
//        given(principal.getUser()).willReturn(user);
//        given(request.getHeader("Origin")).willReturn("http://localhost:3000");
//
//        // When
//        successHandler.onAuthenticationSuccess(request, response, authentication);
//
//        // Then
//        verify(tokenService).generateTokensAndSetHeaders(response, user);
//        verify(response).sendRedirect("http://localhost:3000/home");
//    }
//
//    @Test
//    void newUser_shouldStoreTempUserInRedis_andRedirectToExtraInfo() throws Exception {
//        // Given
//        CustomUserInfo principal = mock(CustomUserInfo.class);
//        OAuth2UserInfo oauth2UserInfo = mock(OAuth2UserInfo.class);
//        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
//
//        given(authentication.getPrincipal()).willReturn(principal);
//        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.NEW_USER);
//        given(principal.getTempUserInfo()).willReturn(oauth2UserInfo);
//        given(principal.getSocial()).willReturn(UserSocial.KAKAO);
//        given(oauth2UserInfo.getEmail()).willReturn("test@example.com");
//        given(oauth2UserInfo.getProviderId()).willReturn("12345");
//        given(oauth2UserInfo.getNickname()).willReturn("nickname");
//        given(redisTemplate.opsForValue()).willReturn(valueOperations);
//        given(request.getHeader("Origin")).willReturn("http://localhost:3000");
//
//        String tempKey = "OAUTH2_TEMP_USER:test-key";
//
//        try (MockedStatic<RedisKeyUtil> mockedStatic = mockStatic(RedisKeyUtil.class)) {
//            mockedStatic.when(RedisKeyUtil::generateOAuth2TempUserKey).thenReturn(tempKey);
//
//            // When
//            successHandler.onAuthenticationSuccess(request, response, authentication);
//
//            // Then
//            verify(valueOperations).set(eq(tempKey), any(TempUserInfo.class), eq(Duration.ofMinutes(10)));
//            verify(response).sendRedirect("http://localhost:3000/extra-info?tempKey=" + tempKey);
//        }
//    }
//
//    @Test
//    void emailDuplicate_shouldRedirectToDuplicateEmailPage() throws Exception {
//        // Given
//        CustomUserInfo principal = mock(CustomUserInfo.class);
//        given(authentication.getPrincipal()).willReturn(principal);
//        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EMAIL_DUPLICATE);
//        given(request.getHeader("Origin")).willReturn("http://localhost:3000");
//
//        // When
//        successHandler.onAuthenticationSuccess(request, response, authentication);
//
//        // Then
//        verify(response).sendRedirect("http://localhost:3000/login/duplicate-email");
//    }
//
//    @Test
//    void noOriginHeader_shouldDefaultToDevUrl() throws Exception {
//        // Given
//        CustomUserInfo principal = mock(CustomUserInfo.class);
//        User user = mock(User.class);
//        given(authentication.getPrincipal()).willReturn(principal);
//        given(principal.getStatus()).willReturn(CustomUserInfo.UserStatus.EXISTING_USER);
//        given(principal.getUser()).willReturn(user);
//        given(request.getHeader("Origin")).willReturn(null); // no origin
//
//        // When
//        successHandler.onAuthenticationSuccess(request, response, authentication);
//
//        // Then
//        verify(response).sendRedirect("http://dev.example.com/home");
//    }
//}
