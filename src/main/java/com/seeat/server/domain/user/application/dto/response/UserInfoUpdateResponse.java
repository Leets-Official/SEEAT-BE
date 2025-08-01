package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.application.dto.response.AuditoriumResponse;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.domain.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(name = "[응답][유저] 유저 정보 수정 목록 Response",description = "유저 마이페이지 정보 수정에 대한 DTO 입니다.")
public record UserInfoUpdateResponse(
        @Schema(description = "유저 닉네임", example = "영화짱")
        String nickname,
        @Schema(description = "유저 프로필 이미지")
        String imageUrl,
        @Schema(description = "유저가 선호하는 영화 장르", example = "[\"ACTION\", \"COMEDY\",\"SF\"]")
        List<MovieGenre>genres,
        @Schema(description = "유저가 선호하는 상영관 Id", example = "[\"1\", \"2\",\"3\"]")
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
