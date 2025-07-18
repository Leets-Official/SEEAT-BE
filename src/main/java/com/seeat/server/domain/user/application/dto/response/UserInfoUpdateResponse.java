package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.domain.entity.User;
import lombok.Builder;

import java.util.List;

@Builder
public record UserInfoUpdateResponse(
        String nickname,
        String imageUrl,
        List<MovieGenre>genres,
        List<Auditorium> auditoriums
) {

    public static  UserInfoUpdateResponse from(User user, List<Auditorium> auditoriums){

        return UserInfoUpdateResponse.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .genres(user.getGenres())
                .auditoriums(auditoriums)
                .build();
    }
}
