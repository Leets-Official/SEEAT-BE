package com.seeat.server.domain.theater.presentation.swagger;

import com.seeat.server.domain.theater.application.dto.response.AuditoriumDetailResponse;
import com.seeat.server.domain.theater.application.dto.response.SeatListResponse;
import com.seeat.server.domain.theater.application.dto.response.TheaterListResponse;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "영화관 API", description = "영화관/상영관/좌석배치도를 호출하는 API 입니다.")
public interface TheaterControllerSpec {

    @Operation(
            summary = "영화관 목록 조회 API",
            description = "해당하는 타입을 가진 영화관 조회"
    )
    ApiResponse<List<TheaterListResponse>> getTheaters(
            @Parameter(
                    description = "조회할 타입"
            )
            @RequestParam AuditoriumType auditoriumType,
            PageRequest pageRequest);

    @Operation(
            summary = "상영관 상세 조회 API",
            description = "상영관 ID를 바탕으로 상영관 조회"
    )
    ApiResponse<AuditoriumDetailResponse> getAuditorium(
            @Parameter(
                    description = "조회할 상영관 Id",
                    example = "13018")
            @PathVariable String auditoriumId);

    @Operation(
            summary = "좌석 배치도 조회 API",
            description = "상영관 ID를 바탕으로 좌석배치도 조회"
    )
    ApiResponse<List<SeatListResponse>> getSeats(
            @Parameter(
                    description = "조회할 상영관 Id",
                    example = "13018")
            @PathVariable String auditoriumId);

}
