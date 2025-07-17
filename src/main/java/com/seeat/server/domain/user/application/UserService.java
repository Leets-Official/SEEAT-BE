package com.seeat.server.domain.user.application;

import com.seeat.server.domain.theater.application.TheaterService;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.domain.entity.*;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserTheaterRepository;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.global.util.JwtConstants;
import com.seeat.server.security.jwt.JwtProvider;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository repository;

    // 외부 의존성
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final TheaterService theaterService;
    private final TheaterRepository theaterRepository;
    private final UserTheaterRepository userTheaterRepository;
    private final AuditoriumRepository auditoriumRepository;


    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    @Override
    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<User> getUserBySocialAndSocialId(UserSocial social, String socialId) {
        return repository.findBySocialAndSocialId(social, socialId);
    }

    @Override
    public void createUser(TempUserInfo tempUserInfo, UserSignUpRequest request) {
        User user = User.of(tempUserInfo.getEmail(), tempUserInfo.getSocialId(), tempUserInfo.getSocial(), tempUserInfo.getUsername(),
                            request.getNickname(), request.getImageUrl(), request.getGenres());

        repository.save(user);

        if (request.getTheaterIds() != null) {
            for (Long theaterId : request.getTheaterIds()) {
                Theater theater = theaterRepository.findById(theaterId)

                        .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_THEATER.getMessage()));

                UserTheater userTheater = UserTheater.of(user, theater);

                userTheaterRepository.save(userTheater);
            }
        }
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();

                    if (jwtProvider.validateToken(refreshToken)) {
                        Authentication authentication = jwtProvider.getAuthentication(refreshToken);
                        User user = (User) authentication.getPrincipal();
                        Long userId = user.getId();
                        redisService.deleteRefreshToken(userId);
                    }

                    Cookie deleteCookie = new Cookie(JwtConstants.REFRESH_TOKEN_COOKIE, null);
                    deleteCookie.setHttpOnly(true);
                    deleteCookie.setSecure(sslEnabled);
                    deleteCookie.setPath("/");
                    deleteCookie.setMaxAge(0);
                    response.addCookie(deleteCookie);
                    break;
                }
            }
        }
    }

    /**
     * 마이페이지 사용자 정보 조회를 위한 로직
     *
     * @param userId 정보를 조회할 사용자 Id
     * @return 사용자 정보에 대한 조회 DTO
     */
    @Override
    public UserInfoResponse getUserInfo(Long userId){

        // 사용자 예외처리 및 사용자 정보 조회
        User user = getUser(userId);

        // 상영관 예외 처리
        // theaterService

        // 선호하는 상영관 조회
        // List<Auditorium> auditoriums

        return UserInfoResponse.from(user);
    }







    //// 공통 함수
    /**
     * @param userId    조회할 유저 Id
     */
    @Override
    public User getUser(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_USER.getMessage()));
    }

}
