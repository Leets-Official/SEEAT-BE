package com.seeat.server.domain.seat.domain;

import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.domain.entity.Theater;

import java.util.UUID;

/**
 * [ 상영관 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class AuditoriumFixtures {

    public static Auditorium createAuditorium(Theater theater) {
        return Auditorium.builder()
                .id("auditorium-id"+ UUID.randomUUID())
                .name("Test Auditorium")
                .screenSize("testScreenSize")
                .soundType("testSoundType")
                .type(AuditoriumType.IMAX)
                .theater(theater)
                .build();
    }



}
