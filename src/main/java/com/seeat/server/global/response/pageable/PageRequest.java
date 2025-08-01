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
@Schema(name = "[요청][공통] 페이지 Request", description = "페이지 요청을 하는 DTO입니다.")
public class PageRequest {
    @Builder.Default
    @Schema(description = "페이지 시작 파라미터. 1부터 시작", example = "1")
    private int page = 1;


    @Builder.Default
    @Schema(description = "가져올 데이터 개수", example = "10")
    private int size = 10;
}
