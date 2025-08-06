package com.seeat.server.domain.best.application.dto.response;

import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithScore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

/**
 * 베스트 상영관 리스트 응답 DTO
 * @param auditoriumId      상영관 ID
 * @param auditoriumName    상영관 이름
 * @param image             상영관 이미지
 * @param avgRating         평균 평점
 * @param reviewCount       총 리뷰 수
 * @param score             점수
 */

@Builder
@Schema(name = "[응답][베스트] 인기 상영관 리스트 Response", description = "베스트 상영관 리스트 조회를 위한 DTO입니다.")
public record BestAuditoriumListResponse(
        @Schema(description = "상영관 ID", example = "13018")
        String auditoriumId,

        @Schema(description = "상영관 이름", example = "IMAX관")
        String auditoriumName,

        @Schema(description = "상영관 이미지", example = "https://seeat-dev.s3.ap-northeast-2.amazonaws.com/official/%E1%84%8B%E1%85%A7%E1%86%BC%E1%84%92%E1%85%AA%E1%84%80%E1%85%AA%E1%86%AB+%E1%84%80%E1%85%A9%E1%86%BC%E1%84%89%E1%85%B5%E1%86%A8+%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5/sample.png")
        String image,

        @Schema(description = "평균 평점", example = "4.7")
        Double avgRating,

        @Schema(description = "총 리뷰 수", example = "324")
        Long reviewCount,

        @Schema(description = "점수", example = "95.5")
        Double score
) {
    /// 정적 팩토리 메서드
    public static BestAuditoriumListResponse from(AuditoriumWithScore withScore) {

        Auditorium auditorium = withScore.getAuditorium();
        Theater theater = auditorium.getTheater();

        return BestAuditoriumListResponse.builder()
                .auditoriumId(auditorium.getId())
                .auditoriumName(theater.getName() + "-" + auditorium.getName())
                .image(auditorium.getThumbnailUrl())
                .avgRating(withScore.getAvgRating())
                .reviewCount(withScore.getReviewCount())
                .score(withScore.getScore())
                .build();
    }

    /// 정적 팩토리 메서드
    public static Slice<BestAuditoriumListResponse> from(Slice<AuditoriumWithScore> withScores) {

        List<BestAuditoriumListResponse> responses = withScores.stream()
                .map(BestAuditoriumListResponse::from)
                .toList();

        return new SliceImpl<>(responses, withScores.getPageable(), withScores.hasNext());

    }

    /// 스냅샷 전용 정적 팩토리 메서드
    public static BestAuditoriumListResponse from(BestAuditoriumSnapshot snapshot) {

        return BestAuditoriumListResponse.builder()
                .auditoriumId(snapshot.getAuditoriumId())
                .auditoriumName(snapshot.getAuditoriumName())
                .image(snapshot.getImage())
                .avgRating(snapshot.getAvgRating())
                .reviewCount(snapshot.getReviewCount())
                .score(snapshot.getScore())
                .build();
    }

    /// 스냅샷 전용 정적 팩토리 메서드
    public static List<BestAuditoriumListResponse> from(List<BestAuditoriumSnapshot> snapshots) {

        return snapshots.stream()
                .map(BestAuditoriumListResponse::from)
                .toList();
    }

}
