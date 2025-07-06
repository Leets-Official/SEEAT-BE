package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.response.OcrResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * [영화 티켓 OCR 인터페이스]
 * - 영화 관람 티켓 이미지에서 텍스트를 추출하는 기능 정의
 */
public interface TicketOcrUseCase {

    /**
     * 이미지 파일에서 텍스트를 추출 (OCR 처리)
     *
     * @param file MultipartFile 형태의 티켓 이미지
     * @return 추출된 텍스트 (ex: 영화 제목, 좌석 등)
     */

    OcrResponse extractText(MultipartFile file) throws Exception;
}
