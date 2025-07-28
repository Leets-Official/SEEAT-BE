package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.application.dto.response.AuditoriumResponse;
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
        List<AuditoriumResponse> auditoriums
) {

    // 정적 메소드
    public static  UserInfoUpdateResponse from(User user, List<Auditorium> auditoriums){

        List<AuditoriumResponse> auditoriumResponses = auditoriums.stream()
                .map(AuditoriumResponse::from)
                .toList();

        return UserInfoUpdateResponse.builder()
                .nickname(user.getNickname())
                .imageUrl(user.getImageUrl())
                .genres(user.getGenres())
                .auditoriums(auditoriumResponses)
                .build();
    }
}
