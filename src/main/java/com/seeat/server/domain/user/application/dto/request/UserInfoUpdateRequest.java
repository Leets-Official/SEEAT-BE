package com.seeat.server.domain.user.application.dto.request;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserInfoUpdateRequest {
    /**
     * 닉네임
     */
    String nickname;

    /**
     * 프로필 이미지
     */
    String imageUrl;

    /**
     * 선호하는 영화 장르
     */
    List<MovieGenre> genres;

    /**
     * 선호하는 상영관
     */
    List<Auditorium> auditoriums;
}
