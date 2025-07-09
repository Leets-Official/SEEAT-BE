package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review.application.dto.response.OcrResponse;
import com.seeat.server.domain.review.presentation.swagger.TicketOcrControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/reviews/ticket")
@RequiredArgsConstructor
public class TicketOcrController implements TicketOcrControllerSpec {

    private final TicketOcrUseCase ocrService;

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<OcrResponse> extractString(
            MultipartFile file) throws Exception {

        return ApiResponse.ok(ocrService.extractText(file));
    }

}
