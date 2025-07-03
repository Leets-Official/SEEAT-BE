package com.seeat.server.domain.user.application.dto;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
public class UserSignUpRequest {
    // 공통응답 머지 후 @NotBlank로 에러처리
    private String nickname;
    private String imageUrl;
    private List<MovieGenre> genres;
    private List<Long> theaterIds;
}
