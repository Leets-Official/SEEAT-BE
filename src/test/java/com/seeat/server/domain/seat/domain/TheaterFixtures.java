package com.seeat.server.domain.seat.domain;

import com.seeat.server.domain.theater.domain.entity.Theater;

/**
 * [ 영화관 도메인을 생성해두는 Fixtures ]입니다.
 * - 테스트에 사용할 도메인들을 미리 정의해두어, 테스트의 가독성을 높여주도록 수행합니다.
 */

public class TheaterFixtures {

   public static Theater createTheater() {
       return Theater.builder()
               .name("Test Theater")
               .address("Test Address")
               .longitude(135.0000)
               .latitude(45.0000)
               .build();
   }


}
