package com.seeat.server.domain.user.application.usecase;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.application.dto.response.UserGradeResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoResponse;
import com.seeat.server.domain.user.application.dto.response.UserInfoUpdateResponse;

import java.util.List;

public interface UserProfileUseCase {

    // 사용자 정보 조회
    UserInfoResponse getUserInfo(Long userId);

    // 사용자 정보 수정
    UserInfoUpdateResponse updateUserInfo(Long userId, String nickName, String imageUrl,
                                          List<MovieGenre> genres, List<Auditorium> auditoriums);

    // 등급 목록 조회
    List<UserGradeResponse> getUserGradeList();
}
