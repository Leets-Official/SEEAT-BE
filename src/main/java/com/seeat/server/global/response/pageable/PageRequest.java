package com.seeat.server.global.response.pageable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

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
