package com.seeat.server.domain.theater.application.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SeatRatingSummaryResponse 에서 사용할 enum 클래스입니다.
 */
@Getter
@RequiredArgsConstructor
public enum SeatType {

    NO_REVIEW("후기가 없는 좌석"),
    REVIEWED("후기가 있는 좌석"),
    HIGH_RATED("평점 높은 좌석"),
    LOW_RATED("평점 낮은 좌석");

    private final String label;

}
