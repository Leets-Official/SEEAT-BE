package com.seeat.server.domain.user.application.dto.request;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 회원가입을 위한 DTO
 * 클라이언트가 회원가입(최초로그인)시 필요한 정보를 담아 서버로 전달할 때 사용합니다.
 * 이미지 추가하는 기능 구현
 */
@Data
@AllArgsConstructor
@Schema(name = "[요청][사용자] 사용자 획원가입 Request", description = "사용자 회원가입에 대한 DTO 입니다.")
public class UserSignUpRequest {

    /**
     * 닉네임
     */
    @NotNull(message = "닉네임은 필수입니다.")
    @Schema(description = "유저 닉네임", example = "팝콘이")
    private String nickname;

    /**
     * 프로필 이미지
     */
    @Schema(description = "유저 프로필 이미지", type = "string", format = "binary")
    private MultipartFile image;

    /**
     * 좋아하는 영화 장르
     */
    @NotNull(message = "좋아하는 영화 장르는 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 장르가 필요합니다.")
    @Schema(description = "유저가 선호하는 영화 장르", example = "[\"ACTION\", \"COMEDY\",\"SF\"]")
    private List<MovieGenre> genres;

    /**
     * 선호하는 상영관 번호 (ex, 1, 2)
     */
    @NotNull(message = "선호 상영관은 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 상영관이 필요합니다.")
    @Schema(description = "유저가 선호하는 상영관 Id", example = "[\"1\", \"2\",\"3\"]")
    private List<String> auditoriumId;
}
