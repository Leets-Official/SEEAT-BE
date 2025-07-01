package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review.presentation.dto.response.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/review/ticket")
@RequiredArgsConstructor
public class TicketOcrController {

    private final TicketOcrUseCase ocrService;

    @PostMapping
    public OcrResponse extractString(MultipartFile file) throws Exception {

        return ocrService.extractText(file);
    }
}
