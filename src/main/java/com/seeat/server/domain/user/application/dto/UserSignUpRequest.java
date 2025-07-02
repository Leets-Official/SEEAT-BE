package com.seeat.server.domain.user.application.dto;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserSignUpRequest {
    private String nickname;
    private String imageUrl;
    private List<MovieGenre> genres;
    private List<Long> theaterIds;
}
