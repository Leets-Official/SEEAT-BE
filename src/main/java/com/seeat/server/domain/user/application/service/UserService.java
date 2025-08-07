package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserNicknameResponse;
import com.seeat.server.domain.user.application.usecase.UserUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserAuditorium;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.domain.user.domain.repository.UserAuditoriumRepository;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.domain.image.application.usecase.ImageUseCase;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.service.RedisService;
import com.seeat.server.global.util.JwtConstants;
import com.seeat.server.security.jwt.JwtProvider;
import com.seeat.server.security.oauth2.application.dto.TempUserInfo;
import com.seeat.server.security.oauth2.application.dto.response.CustomUserInfo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository repository;

    // 외부 의존성
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final UserAuditoriumRepository userAuditoriumRepository;
    private final AuditoriumRepository auditoriumRepository;

    @Value("${server.ssl.enabled}")
    private boolean sslEnabled;

    /**
     * 이메일로 중복 확인 로직
     *
     * @param email 최초 로그인하는 이메일
     * @return User 객체
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * 닉네임 중복 확인 로직
     *
     * @param nickname 사용할 닉네임
     * @return true, false
     */
    @Override
    public UserNicknameResponse isNicknameDuplicated (String nickname){

        Boolean response = repository.existsByNickname(nickname);

        // 중복이면 에러처리
        if (response){

            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME, null);
        }

        return UserNicknameResponse.from(repository.existsByNickname(nickname));
    }


    /**
     * 가입한 소셜 종류와 소셜 ID으로 최초 로그인인지 확인 로직
     *
     * @param social 가입한 소셜 종류
     * @param socialId 소셜 ID
     * @return User 객체
     */
    public Optional<User> getUserBySocialAndSocialId(UserSocial social, String socialId) {
        return repository.findBySocialAndSocialId(social, socialId);
    }

    /**
     * 최초 가입 회원가입을 위한 로직
     *
     * @param tempUserInfo 임시유저 정보
     * @param request 회원가입을 위한 추가 정보
     */
    @Override
    public User createUser(TempUserInfo tempUserInfo, UserSignUpRequest request) throws IOException {

        String thumbnailImage = "thumbnail";

        /// 존재한다면 이미지 추가
        if (request.getImage() != null) {
            thumbnailImage = request.getImage();
        }
        /// 유저 객체 생성
        User requestUser = User.of(tempUserInfo.getEmail(), tempUserInfo.getSocialId(), tempUserInfo.getSocial(), tempUserInfo.getUsername(),
                request.getNickname(), thumbnailImage, request.getGenres());

        /// DB에 유저 저장
        User user = repository.save(requestUser);

        // 선호하는 상영관 유무 체크 후 저장
        if (request.getAuditoriumId() != null) {
            for (String auditoriumId : request.getAuditoriumId()) {
                Auditorium auditorium = auditoriumRepository.findById(auditoriumId)

                        .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage()));

                UserAuditorium userAuditorium = UserAuditorium.of(user, auditorium);

                userAuditoriumRepository.save(userAuditorium);
            }
        }

        /// 인증 객체 생성 및 저장
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().getRole()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return user;
    }

    /**
     * 로그아웃을 위한 로직
     *
     * @param request  HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtConstants.REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();

                    // 유효성 체크 후 삭제
                    if (jwtProvider.validateToken(refreshToken)) {
                        Authentication authentication = jwtProvider.getAuthentication(refreshToken);
                        CustomUserInfo userInfo = (CustomUserInfo) authentication.getPrincipal();

                        Long userId = userInfo.getUser().getId();

                        // refreshToken 삭제
                        redisService.deleteRefreshToken(userId);
                    }

                    // 쿠키 삭제
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
        return repository.findByIdAndIsDeleteFalse(userId)
                .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_USER.getMessage()));
    }

}
