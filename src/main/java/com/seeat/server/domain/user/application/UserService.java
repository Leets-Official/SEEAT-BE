package com.seeat.server.domain.user.application;

import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.domain.user.domain.entity.*;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserTheaterRepository;
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

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository repository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    private final TheaterRepository theaterRepository;
    private final UserTheaterRepository userTheaterRepository;


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
            for (String theaterId : request.getTheaterIds()) {
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
