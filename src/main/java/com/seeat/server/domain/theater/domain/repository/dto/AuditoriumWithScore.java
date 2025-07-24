package com.seeat.server.domain.theater.domain.repository.dto;

import com.seeat.server.domain.theater.domain.entity.Auditorium;

public interface AuditoriumWithScore {

    Auditorium getAuditorium();
    Long getReviewCount();
    Double getAvgRating();
    Double getScore();

}
