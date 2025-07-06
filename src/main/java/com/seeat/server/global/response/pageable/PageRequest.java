package com.seeat.server.global.response.pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


/**
 * 페이지네이션을 위한 요청 클래스
 * - @Schema 를 통해 스웨거에서 기본적으로 값을 지정해두었습니다.
 */

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PageRequest {
    @Builder.Default
    @Schema(example = "1")
    private int page = 1;

    @Builder.Default
    @Schema(example = "10")
    private int size = 10;
}
