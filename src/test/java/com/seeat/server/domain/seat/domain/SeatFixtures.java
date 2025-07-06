package com.seeat.server.domain.seat.domain;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;

/**
 * [ 좌석 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class SeatFixtures {

    public static Seat createSeat(Auditorium auditorium) {
        return Seat.builder()
                .auditorium(auditorium)
                .row("testRow")
                .column("testColumn")
                .build();
    }

}
