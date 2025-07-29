package com.seeat.server.domain.best.application.util;

import com.seeat.server.domain.best.application.dto.response.BestAuditoriumListResponse;
import com.seeat.server.domain.best.application.dto.response.BestReviewListResponse;
import com.seeat.server.domain.best.domain.entity.BestAuditoriumSnapshot;
import com.seeat.server.domain.best.domain.entity.BestReviewSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 스냅샷 변환 유틸리티 클래스
 */
@Component
public class SnapShotConverter {

    /**
     * ReviewListResponse -> BestReviewSnapshot 변환
     */
    public static BestReviewSnapshot toReviewSnapshot(BestReviewListResponse response) {
        return BestReviewSnapshot.of(
                response.reviewId(),
                response.hashtags(),
                response.thumbnailUrl(),
                response.movieTitle(),
                response.theaterName(),
                response.content(),
                response.userId(),
                response.nickname(),
                response.profileImageUrl(),
                response.heartCount(),
                response.createdAt()
        );
    }

    /**
     * List<ReviewListResponse> -> List<BestReviewSnapshot> 변환
     */
    public static List<BestReviewSnapshot> toReviewSnapshot(List<BestReviewListResponse> responses) {
        return responses.stream()
                .map(SnapShotConverter::toReviewSnapshot)
                .toList();
    }

    /**
     * BestAuditoriumListResponse -> BestAuditoriumSnapshot 변환
     */
    public static BestAuditoriumSnapshot toAuditoriumSnapshot(BestAuditoriumListResponse response) {
        return BestAuditoriumSnapshot.of(
                response.auditoriumId(),
                response.auditoriumName(),
                response.avgRating(),
                response.reviewCount(),
                response.score()
        );
    }

    /**
     * List<BestAuditoriumListResponse> -> List<BestAuditoriumSnapshot> 변환
     */
    public static List<BestAuditoriumSnapshot> toAuditoriumSnapshot(List<BestAuditoriumListResponse> responses) {
        return responses.stream()
                .map(SnapShotConverter::toAuditoriumSnapshot)
                .toList();
    }
}
