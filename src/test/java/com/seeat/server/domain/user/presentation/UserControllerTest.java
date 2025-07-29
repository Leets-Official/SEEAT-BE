package com.seeat.server.domain.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.domain.theater.domain.AuditoriumFixtures;
import com.seeat.server.domain.theater.domain.TheaterFixtures;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.domain.UserFixtures;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserRole;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserAuditoriumRepository;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.jwt.JwtProvider;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    private AuditoriumRepository auditoriumRepository;

    @Autowired TheaterRepository theaterRepository;

    @Autowired
    private UserAuditoriumRepository userAuditoriumRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtProvider jwtProvider;

    private Auditorium auditorium1;
    private Auditorium auditorium2;
    private MockMultipartFile file1;

    @BeforeEach
    void setUp() throws IOException {
        Theater theater1 = theaterRepository.save(TheaterFixtures.createTheater());
        Theater theater2 = theaterRepository.save(TheaterFixtures.createTheater());

        auditorium1 = AuditoriumFixtures.createAuditorium(theater1, "theater1");
        auditorium2 = AuditoriumFixtures.createAuditorium(theater2, "theater2");

        auditoriumRepository.saveAll(List.of(auditorium1, auditorium2));

        /// 파일 저장
        InputStream inputStream1 = getClass().getClassLoader().getResourceAsStream("static/testImage1.png");
        file1 = new MockMultipartFile("imageUrls", "sample1.png", MediaType.IMAGE_PNG_VALUE, inputStream1);
    }

    @Test
    void givenTempUserKeyAndSignUpRequest_whenPostUserSignUp_thenReturnsOk() throws Exception {
        // Given
        String tempUserKey = "OAUTH2_TEMP_USER:abc123";
        TempUserInfo tempUserInfo = new TempUserInfo("test@example.com", "providerId123", UserSocial.KAKAO, "username");

        given(redisService.getValues(tempUserKey, TempUserInfo.class)).willReturn(tempUserInfo);
        willDoNothing().given(redisService).deleteValues(tempUserKey);

        // When
        mockMvc.perform(multipart("/api/v1/users")
                        .file(file1) // 이미지 파일
                        .param("nickname", "nickname") // 닉네임
                        .param("genres", "ROMANCE", "ACTION") // 장르 여러 개
                        .param("auditoriumId", auditorium1.getId(), auditorium2.getId()) // 상영관 ID 여러 개
                        .header("Temp-User-Key", tempUserKey)
                        .characterEncoding("UTF-8")
                )
                .andExpect(status().isOk());

        // Then
        var savedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertEquals("nickname", savedUser.getNickname());
        Assertions.assertThat(savedUser.getImageUrl().contains("sample"));
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("providerId123", savedUser.getSocialId());
        assertEquals(UserSocial.KAKAO, savedUser.getSocial());
        assertEquals("username", savedUser.getUsername());
        assertIterableEquals(List.of(MovieGenre.ROMANCE, MovieGenre.ACTION), savedUser.getGenres());

        List<MovieGenre> expectedGenres = List.of(MovieGenre.ROMANCE, MovieGenre.ACTION);
        assertIterableEquals(expectedGenres, savedUser.getGenres());

        List<Auditorium> auditoriums = userAuditoriumRepository.findDistinctAuditoriumsByUserId(savedUser.getId());
        List<String> savedAuditoriumIds = auditoriums.stream()
                .map(ut -> ut.getId())
                .collect(Collectors.toList());

        List<String> expectedAuditoriumIds = List.of(auditorium1.getId(), auditorium2.getId());
        assertIterableEquals(expectedAuditoriumIds, savedAuditoriumIds);
    }

    @Test
    void givenValidRequest_whenPostLogout_thenReturnsOkAndCallsLogoutService() throws Exception {
        // Given
        String refreshToken = "validRefreshToken";

        // 실제 유저 엔티티 생성
        User user = UserFixtures.fakeUser();

        // 권한 포함한 Authentication 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(UserRole.USER.getRole()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, authorities);

        // JwtProvider, RedisService 모킹
        given(jwtProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtProvider.getAuthentication(refreshToken)).willReturn(authentication);
        given(redisService.getRefreshToken(user.getId())).willReturn(refreshToken);
        doNothing().when(redisService).deleteRefreshToken(user.getId());

        // When & Then
        mockMvc.perform(post("/api/v1/users/logout")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(content().json("""
            {
                "success": true,
                "code": 200,
                "message": "호출이 성공적으로 완료되었습니다.",
                "data": null,
                "error": null
            }
            """));

        verify(redisService, times(1)).deleteRefreshToken(user.getId());
    }



}
