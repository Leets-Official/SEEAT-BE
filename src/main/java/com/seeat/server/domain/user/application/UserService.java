package com.seeat.server.domain.user.application;

import com.seeat.server.domain.theater.application.TheaterService;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.theater.domain.repository.TheaterRepository;
import com.seeat.server.domain.user.application.dto.request.UserSignUpRequest;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import com.seeat.server.domain.user.domain.entity.UserTheater;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void createUser(TempUserInfo tempUserInfo, UserSignUpRequest request) {
        // 유저 DB에 저장
        User user = User.of(tempUserInfo.getEmail(), tempUserInfo.getSocialId(), tempUserInfo.getSocial(), tempUserInfo.getUsername(),
                            request.getNickname(), request.getImageUrl(), request.getGenres());

        repository.save(user);

        // 선호하는 상영관 유무 체크 후 저장
        if (request.getTheaterIds() != null) {
            for (Long theaterId : request.getTheaterIds()) {
                Theater theater = theaterRepository.findById(theaterId)

                        .orElseThrow(() -> new NoSuchElementException(ErrorCode.NOT_THEATER.getMessage()));

                UserTheater userTheater = UserTheater.of(user, theater);

                userTheaterRepository.save(userTheater);
            }
        }
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
                        User user = (User) authentication.getPrincipal();
                        Long userId = user.getId();

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

    /**
     * 마이페이지 사용자 정보 수정을 위한 로직
     *
     * @param userId 정보를 수정할 사용자 Id
     * @param nickName 수정할 닉네임
     * @param imageUrl 수정할 imageUrl
     * @param genres 수정할 장르
     * @param auditoriums 수정할 선호 상영관
     * @return 업데이트된 사용자 정보에 대한 DTO
     */
    @Override
    public UserInfoUpdateResponse updateUserInfo(Long userId, String nickName, String imageUrl,
                                          List<MovieGenre> genres, List<Auditorium> auditoriums){

        // 사용자 예외 처리
        User user = getUser(userId);

        // 상영관 예외 처리

        // 사용자 정보 수정
        user.updateUser(nickName, imageUrl, genres);

        // 상영관 업데이트

        // 사용자 업데이트 DTO로 변환
        return UserInfoUpdateResponse.from(user, auditoriums);
    }

    /**
     * 사용자 등급 목록 조회를 위한 로직
     *
     * @return UserGradeResponse DTO List 응답
     */
    @Override
    public List<UserGradeResponse> getUserGradeList(){

        // 유저 등급 목록 조회
        return Arrays.stream(UserGrade.values())
                .map(UserGradeResponse::new)
                .collect(Collectors.toList());
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
