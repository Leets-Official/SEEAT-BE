package com.seeat.server.domain.user.application.dto.request;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(name = "[요청][사용자] 사용자 정보 수정 Request", description = "사용자 정보 수정에 대한 DTO 입니다.")
public class UserInfoUpdateRequest {
    /**
     * 닉네임
     */
    @Schema(description = "유저 닉네임", example = "무비매니아")
    String nickname;

    /**
     * 프로필 이미지
     */
    @Schema(description = "유저 프로필 이미지", type = "string", format = "binary")
    MultipartFile image;

    /**
     * 선호하는 영화 장르
     */
    @Schema(description = "유저가 선호하는 영화 장르", example = "[\"ACTION\", \"COMEDY\",\"SF\"]")
    List<MovieGenre> genres;

    /**
     * 선호하는 상영관
     */
    @Schema(description = "유저가 선호하는 상영관 Id", example = "[\"1\", \"2\",\"3\"]")
    List<String> auditoriumIds;
}
