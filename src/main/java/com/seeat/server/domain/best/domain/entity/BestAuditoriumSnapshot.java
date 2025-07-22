package com.seeat.server.domain.best.domain.entity;

import com.seeat.server.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class BestAuditoriumSnapshot extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String auditoriumId;
    private String auditoriumName;

    private double avgRating;
    private int reviewCount;
    private double score;

}
