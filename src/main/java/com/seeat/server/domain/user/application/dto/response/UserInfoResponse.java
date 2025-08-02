package com.seeat.server.domain.user.application.dto.response;

import com.seeat.server.domain.theater.application.dto.response.AuditoriumResponse;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.domain.user.domain.entity.User;
import com.seeat.server.domain.user.domain.entity.UserGrade;
import com.seeat.server.domain.user.domain.entity.UserSocial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(name = "[응답][유저] 유저 정보 조회 목록 Response",description = "유저 마이페이지 정보 조회에 대한 DTO 입니다.")
public record UserInfoResponse(
        @Schema(description = "유저 고유 ID", example = "1")
        Long id,
        @Schema(description = "유저 이메일", example = "seeat@google.co.kr")
        String email,
        @Schema(description = "유저 고유 소셜 ID", example = "1234")
        String socialId,
        @Schema(description = "사용자 이름", example = "김영화")
        String username,
        @Schema(description = "유저 닉네임", example = "영화짱")
        String nickname,
        @Schema(description = "유저 프로필 이미지")
        String imageUrl,
        @Schema(description = "유저 등급", example = "BRONZE")
        UserGrade grade,
        @Schema(description = "유저가 선호하는 영화 장르", example = "[\"ACTION\", \"COMEDY\",\"SF\"]")
        List<MovieGenre> genres,
        @Schema(description = "유저가 가입한 소셜 루트", example = "KAKAO")
        UserSocial social,
        @Schema(description = "유저가 선호하는 상영관 Id", example = "[\"1\", \"2\",\"3\"]")
        List<AuditoriumResponse> auditoriums,
        @Schema(description = "작성한 리뷰 개수", example = "1")
        long reviewCount,
        @Schema(description = "받은 좋아요 개수", example = "3")
        long likeCount,

        /**
         * 레벨 1 → 레벨2 : 후기 2개 / 좋아요 5개
         * 레벨2 → 레벨3: 후기 10개 / 좋아요 25개
         * 레벨3 → 레벨4: 후기 40개 / 좋아요 100개
         *
         * (레벨 올라가는 후기/좋아요 개수는 이전 레벨에서 누른 것도 포함)
         *
         */
        @Schema(description = "개수에 따른 경험치", example = "")
        double levelExp

) {
    public static UserInfoResponse from(User user ,List<Auditorium> auditoriums,
                                        long reviewCount, long likeCount, double levelExp){

        List<AuditoriumResponse> auditoriumResponses = auditoriums.stream()
                .map(AuditoriumResponse::from)
                .toList();

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
                .auditoriums(auditoriumResponses)
                .reviewCount(reviewCount)
                .likeCount(likeCount)
                .levelExp(levelExp)
                .build();
    }
}
