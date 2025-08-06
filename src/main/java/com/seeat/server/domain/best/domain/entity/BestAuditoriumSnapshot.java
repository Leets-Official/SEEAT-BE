package com.seeat.server.domain.best.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BestAuditoriumSnapshot extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String auditoriumId;
    private String auditoriumName;
    private String image;
    private Double avgRating;
    private Long reviewCount;
    private Double score;

    /// 정적 팩토리 메서드 생성
    public static BestAuditoriumSnapshot of(String auditoriumId, String auditoriumName, Double avgRating, Long reviewCount, Double score, String image) {
        return BestAuditoriumSnapshot.builder()
                .auditoriumId(auditoriumId)
                .auditoriumName(auditoriumName)
                .image(image)
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .score(score)
                .build();

    }

}
