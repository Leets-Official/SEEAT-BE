package com.seeat.server.domain.user.application.service;

import com.seeat.server.domain.theater.application.TheaterService;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.theater.domain.repository.AuditoriumRepository;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;
import com.seeat.server.domain.user.application.usecase.UserProfileUseCase;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.repository.UserAuditoriumRepository;
import com.seeat.server.domain.user.domain.repository.UserRepository;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUseCase {

    private final UserRepository repository;

    private final UserService service;

    // 외부 의존성
    private final TheaterService theaterService;
    private final UserAuditoriumRepository userAuditoriumRepository;
    private final AuditoriumRepository auditoriumRepository;

    /**
     * 마이페이지 사용자 정보 조회를 위한 로직
     *
     * @param userId 정보를 조회할 사용자 Id
     * @return 사용자 정보에 대한 조회 DTO
     */
    @Override
    public UserInfoResponse getUserInfo(Long userId){

        // 사용자 예외처리 및 사용자 정보 조회
        User user = service.getUser(userId);

        // 상영관 n+1 방지 fetch join
        List<Auditorium> auditorium = userAuditoriumRepository.findDistinctAuditoriumsByUserId(userId);

        // 예외 처리
        if (auditorium.isEmpty()) {
            throw new NoSuchElementException(ErrorCode.NOT_AUDITORIUM.getMessage());
        }

        return UserInfoResponse.from(user, auditorium);
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
        User user = service.getUser(userId);

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

}
