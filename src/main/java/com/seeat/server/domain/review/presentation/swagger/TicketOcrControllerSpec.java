package com.seeat.server.domain.review.presentation.swagger;

import com.seeat.server.domain.review.application.dto.response.OcrResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "티켓 OCR API")
public interface TicketOcrControllerSpec {

    @Operation(
            summary = "OCR 호출 API",
            description = "모바일 티켓 이미지를 바탕으로 추출하는 API 입니다."
    )
    ApiResponse<OcrResponse> extractString(
            @RequestPart MultipartFile file) throws Exception;
}
