package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;

import java.util.List;

public record UserInfoUpdateResponse(
        String nickname,

        String imageUrl,

        List<MovieGenre>genres,

        List<Auditorium> auditoriums
) {
}
