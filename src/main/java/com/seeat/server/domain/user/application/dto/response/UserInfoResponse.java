package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import lombok.Builder;

import java.util.List;

@Builder
public record UserInfoResponse(
        Long id,
        String email,
        String socialId,
        String username,
        String nickname,
        String imageUrl,
        UserGrade grade,
        List<MovieGenre> genres,
        UserSocial social

       // List<Auditorium> auditoriums
) {
    public static UserInfoResponse from(User user
            /*, List<Auditorium> auditoriums*/){

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .socialId(user.getSocialId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .grade(user.getGrade())
                .genres(user.getGenres())
                .social(user.getSocial())
                // .auditoriums(auditoriums)
                .build();
    }
}
