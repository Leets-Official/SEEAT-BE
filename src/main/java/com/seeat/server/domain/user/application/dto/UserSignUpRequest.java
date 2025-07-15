package com.seeat.server.domain.user.application.dto;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 회원가입을 위한 DTO
 * 클라이언트가 회원가입(최초로그인)시 필요한 정보를 담아 서버로 전달할 때 사용합니다.
 */
@Data
@AllArgsConstructor
public class UserSignUpRequest {

    /**
     * 닉네임
     */
    @NotNull(message = "닉네임은 필수입니다.")
    private String nickname;

    /**
     * 프로필 이미지
     */
    @NotNull(message = "프로필 이미지는 필수입니다.")
    private String imageUrl;

    /**
     * 좋아하는 영화 장르
     */
    @NotNull(message = "좋아하는 영화 장르는 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 장르가 필요합니다.")
    private List<MovieGenre> genres;

    /**
     * 선호하는 영화관 번호 (ex, 1, 2)
     */
    @NotNull(message = "선호 영화관은 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 영화관이 필요합니다.")
    private List<String> theaterIds;
}
