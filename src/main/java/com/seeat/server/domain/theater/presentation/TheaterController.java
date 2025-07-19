package com.seeat.server.domain.theater.presentation;

import com.seeat.server.domain.theater.application.TheaterUseCase;
import com.seeat.server.domain.theater.application.dto.response.AuditoriumDetailResponse;
import com.seeat.server.domain.theater.application.dto.response.SeatListResponse;
import com.seeat.server.domain.theater.application.dto.response.TheaterListResponse;
import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.domain.theater.presentation.swagger.TheaterControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.pageable.PageRequest;
import com.seeat.server.global.response.pageable.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/theaters")
public class TheaterController implements TheaterControllerSpec {

    private final TheaterUseCase theaterService;

    /**
     * 상영관 목록 조회
     * @param auditoriumType    상영관 타입
     */

    @GetMapping()
    public ApiResponse<SliceResponse<TheaterListResponse>> getTheaters(
            @RequestParam AuditoriumType auditoriumType,
            PageRequest pageRequest) {

        /// 서비스 호출
        SliceResponse<TheaterListResponse> response = theaterService.loadTheatersByType(auditoriumType, pageRequest);

        return ApiResponse.ok(response);
    }

    /**
     * 상영관 상세 조회 API
     * @param auditoriumId  상영관 ID
     */
    @GetMapping("/auditorium/{auditoriumId}")
    public ApiResponse<AuditoriumDetailResponse> getAuditorium(@PathVariable String auditoriumId) {

        /// 서비스 호출
        AuditoriumDetailResponse response = theaterService.loadAuditorium(auditoriumId);

        return ApiResponse.ok(response);
    }

    /**
     * 좌석 배치도 조회 API
     * @param auditoriumId  상영관 ID
     */
    @GetMapping("/seat/{auditoriumId}")
    public ApiResponse<List<SeatListResponse>> getSeats(@PathVariable String auditoriumId) {

        /// 서비스 호출
        List<SeatListResponse> responses = theaterService.loadSeatsByAuditorium(auditoriumId);

        return ApiResponse.ok(responses);
    }

}
