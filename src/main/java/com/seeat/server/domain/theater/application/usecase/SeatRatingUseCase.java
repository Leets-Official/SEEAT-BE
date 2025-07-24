package com.seeat.server.domain.theater.application.usecase;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.theater.application.dto.response.SeatRatingSummaryResponse;
import com.seeat.server.domain.theater.domain.entity.Seat;

import java.util.*;

/**
 * 좌석 평가 관련 유스케이스 인터페이스입니다.
 */
public interface SeatRatingUseCase {

    /// 리뷰와 관련된 상영관에 해당하는 좌석 배치도를 제공
    List<SeatRatingSummaryResponse> getSeatRatingSummariesByAuditoriumId(String auditoriumId);

    /// 외부 서비스 사용 함수
    // 리뷰 추가될 때마다 정보 업데이트하기
    void saveSeatRating(Review review, Seat seat);

}
