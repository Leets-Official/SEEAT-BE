package com.seeat.server.domain.best.application.dto.response;

import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.domain.theater.domain.repository.dto.AuditoriumWithScore;
import lombok.Builder;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

/**
 * 베스트 상영관 리스트 응답 DTO
 * @param auditoriumId      상영관 ID
 * @param auditoriumName    상영관 이름
 * @param avgRating         평균 평점
 * @param reviewCount       총 리뷰 수
 * @param score             점수
 */

@Builder
public record BestAuditoriumListResponse(
        String auditoriumId,
        String auditoriumName,
        Double avgRating,
        Long reviewCount,
        Double score
) {
    /// 정적 팩토리 메서드
    public static BestAuditoriumListResponse from(AuditoriumWithScore withScore) {

        Auditorium auditorium = withScore.getAuditorium();
        Theater theater = auditorium.getTheater();

        return BestAuditoriumListResponse.builder()
                .auditoriumId(auditorium.getId())
                .auditoriumName(theater.getName() + "-" + auditorium.getName())
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
