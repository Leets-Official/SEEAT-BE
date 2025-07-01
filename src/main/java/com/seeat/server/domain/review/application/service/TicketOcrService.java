package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review.external.NaverOcrApi;
import com.seeat.server.domain.review.presentation.dto.response.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 티켓 OCR 서비스 구현체.
 *
 * 파일 유효성 검사, 임시 파일 저장, OCR API 호출 및 결과 파싱을 담당합니다.
 * 컨트롤러에서는 예외를 받아 적절한 HTTP 응답으로 변환합니다.
 */

@Service
@RequiredArgsConstructor
public class TicketOcrService implements TicketOcrUseCase {

    private final NaverOcrApi naverOcrApi;

    @Override
    public OcrResponse extractText(MultipartFile file) throws Exception {
        // 파일 존재 여부 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 첨부되지 않았습니다.");
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        } else {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }

        // 임시 파일로 저장
        File tempFile = null;
        try {
            tempFile = File.createTempFile("ocr_", "." + ext);
            file.transferTo(tempFile);

            // OCR API 호출 (텍스트 추출 결과 리스트)
            List<String> result = naverOcrApi.callApi(HttpMethod.POST.name(), tempFile.getAbsolutePath(), ext);

            // OCR 결과를 DTO로 변환 (파싱 로직은 OcrResponse.from에서 처리)
            return OcrResponse.from(result);

        } finally {
            // 임시 파일 삭제 (예외 발생 여부와 관계없이 항상 삭제)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
