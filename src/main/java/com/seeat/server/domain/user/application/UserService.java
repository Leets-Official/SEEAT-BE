package com.seeat.server.domain.user.application;

import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.UserSignUpRequest;
import com.seeat.server.domain.user.domain.entity.*;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.user.domain.repository.UserTheaterRepository;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.security.jwt.JwtProvider;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final TheaterRepository theaterRepository;
    private final UserTheaterRepository userTheaterRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserBySocialAndSocialId(UserSocial social, String socialId){
        return userRepository.findBySocialAndSocialId(social, socialId);
    }

    public void createUser(TempUserInfo tempUserInfo, UserSignUpRequest request){
        User user = User.builder()
                .email(tempUserInfo.getEmail())
                .socialId(tempUserInfo.getSocialId())
                .social(tempUserInfo.getSocial())
                .username(tempUserInfo.getUsername())
                .nickname(request.getNickname())
                .imageUrl(request.getImageUrl())
                .genres(request.getGenres())
                .role(UserRole.USER)
                .grade(UserGrade.BRONZE)
                .build();

        userRepository.save(user);

        if (request.getTheaterIds() != null) {
            for (Long theaterId : request.getTheaterIds()) {
                Theater theater = theaterRepository.findById(theaterId)
                        // 공통에러처리
                        .orElseThrow(() -> new RuntimeException("극장을 찾을 수 없습니다. id: " + theaterId));

                UserTheater userTheater = UserTheater.builder()
                        .user(user)
                        .theater(theater)
                        .isMainTheater(false)
                        .build();

                userTheaterRepository.save(userTheater);
            }
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();

                    if (jwtProvider.validateToken(refreshToken)) {
                        Authentication authentication = jwtProvider.getAuthentication(refreshToken);
                        Long userId = Long.parseLong(authentication.getName());

                        redisService.deleteRefreshToken(userId);
                    }

                    Cookie deleteCookie = new Cookie("refreshToken", null);
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
}
