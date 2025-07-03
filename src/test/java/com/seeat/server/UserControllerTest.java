package com.seeat.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.UserService;
import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.domain.user.domain.entity.UserTheater;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserTheaterRepository;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.jwt.JwtProvider;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedisService redisService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private UserTheaterRepository userTheaterRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        theaterRepository.save(Theater.builder()
                .name("테스트 극장 10")
                .address("서울시 강남구 10")
                .latitude(37.123)
                .longitude(127.123)
                .build());

        theaterRepository.save(Theater.builder()
                .name("테스트 극장 30")
                .address("서울시 강남구 30")
                .latitude(37.456)
                .longitude(127.456)
                .build());
    }
    @Test
    void givenTempUserKeyAndSignUpRequest_whenPostUserSignUp_thenReturnsOk() throws Exception {
        // Given
        String tempUserKey = "OAUTH2_TEMP_USER:abc123";
        TempUserInfo tempUserInfo = new TempUserInfo("test@example.com", "providerId123", UserSocial.KAKAO, "username");
        UserSignUpRequest request = new UserSignUpRequest(
                "nickname",
                "https://example.com/profile.jpg",
                List.of(MovieGenre.ROMANCE, MovieGenre.ACTION),
                List.of(1L, 2L)
        );

        given(redisService.getValues(tempUserKey, TempUserInfo.class)).willReturn(tempUserInfo);
        willDoNothing().given(redisService).deleteValues(tempUserKey);

        // When
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Temp-User-Key", tempUserKey)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then
        var savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertEquals("nickname", savedUser.getNickname());
        assertEquals("https://example.com/profile.jpg", savedUser.getImageUrl());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("providerId123", savedUser.getSocialId());
        assertEquals(UserSocial.KAKAO, savedUser.getSocial());
        assertEquals("username", savedUser.getUsername());
        assertIterableEquals(List.of(MovieGenre.ROMANCE, MovieGenre.ACTION), savedUser.getGenres());

        List<MovieGenre> expectedGenres = List.of(MovieGenre.ROMANCE, MovieGenre.ACTION);
        assertIterableEquals(expectedGenres, savedUser.getGenres());

        List<UserTheater> userTheaters = userTheaterRepository.findByUserId(savedUser.getId());
        List<Long> savedTheaterIds = userTheaters.stream()
                .map(ut -> ut.getTheater().getId())
                .collect(Collectors.toList());

        List<Long> expectedTheaterIds = List.of(1L, 2L);
        assertIterableEquals(expectedTheaterIds, savedTheaterIds);
    }

    @Test
    void givenValidRequest_whenPostLogout_thenReturnsOkAndCallsLogoutService() throws Exception {
        // Given
        String refreshToken = "validRefreshToken";
        Long userId = 123L;

        given(jwtProvider.validateToken(refreshToken)).willReturn(true);

        Authentication authentication = mock(Authentication.class);
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);
        given(authentication.getPrincipal()).willReturn(mockUser);
        given(jwtProvider.getAuthentication(refreshToken)).willReturn(authentication);

        doNothing().when(redisService).deleteRefreshToken(userId);

        // When
        ResultActions result = mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new Cookie("refreshToken", refreshToken))
                        .with(user("user").password("pass").roles("USER"))) // 인증 정보 주입
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(content().string("로그아웃 완료"));

        // Then
        verify(redisService, times(1)).deleteRefreshToken(userId);
    }
}
