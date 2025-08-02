package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review.external.NaverOcrApi;
import com.seeat.server.domain.review.application.dto.response.OcrResponse;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * 티켓 OCR 서비스 구현체.
 * <p>
 * 파일 유효성 검사, 임시 파일 저장, OCR API 호출 및 결과 파싱을 담당합니다.
 * 컨트롤러에서는 예외를 받아 적절한 HTTP 응답으로 변환합니다.
 */

@Service
@RequiredArgsConstructor
public class TicketOcrService implements TicketOcrUseCase {

    private final NaverOcrApi naverOcrApi;

    /// 외부 의존성
    private final TheaterUseCase theaterService;

    @Override
    public OcrResponse extractText(MultipartFile file) throws Exception {
        /// 파일 존재 여부 체크
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.NOT_OCR_IMAGE.getMessage());
        }

        /// 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        } else {
            throw new IllegalArgumentException(ErrorCode.BAD_OCR_IMAGE.getMessage());
        }

        // 임시 파일로 저장
        File tempFile = null;
        try {
            tempFile = File.createTempFile("ocr_", "." + ext);
            file.transferTo(tempFile);

            /// OCR API 호출 (텍스트 추출 결과 리스트)
            List<String> result = naverOcrApi.callApi(HttpMethod.POST.name(), tempFile.getAbsolutePath(), ext);

            /// OCR 결과를 분기
            List<String> strings = cgvImageOCR(result);

            /// 분기한 것을 DTO로 변환
            return toResponse(strings);

        } finally {
            // 임시 파일 삭제 (예외 발생 여부와 관계없이 항상 삭제)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    ///
    private List<String> cgvImageOCR(List<String> strings) {

        /// 예외 처리
        if (strings == null || strings.size() < 3) {
            return null;
        }

        String titleInfo = strings.get(0);
        String hallInfo = strings.get(1);
        String seatInfo = strings.get(2);

        /// 영화제목 정보 추출
        String[] titleParts = titleInfo.split("\n");
        String title = titleParts.length > 0 ? titleParts[0] : null;

        /// 상영관 정보 추출
        String[] hallLines = hallInfo.split("\n");
        String theater = hallLines.length > 0 ? "CGV " + hallLines[0] : null;

        /// "IMAX관 7층" 같은 줄에서 "IMAX관"만 추출
        String hallLine = hallLines.length > 1 ? hallLines[1] : null;
        String hall = null;
        if (hallLine != null) {
            hall = hallLine.split(" ")[0];
        }

        /// 좌석 정보 추출
        String[] seatLines = seatInfo.split("\n");
        String seat = seatLines.length > 0 ? seatLines[0] : null;

        return List.of(theater, title, hall, seat);
    }



    /// DB에서 결과 가져오기
    private OcrResponse toResponse(List<String> strings) {
        if (strings == null || strings.size() < 3) {
            return null;
        }

        /// DB에서 해당 값들 조회
        String theater = strings.get(0);
        String title = strings.get(1);
        String hall = strings.get(2);
        String seat = strings.get(3);

        /// 체크
        Seat checkSeat = theaterService.getSeatByName(theater, hall, seat);
        Auditorium checkAuditorium = checkSeat.getAuditorium();
        Theater checkTheater = checkAuditorium.getTheater();

        /// DTO 변환
        return OcrResponse.from(checkTheater.getName(), title, checkAuditorium.getId(), checkAuditorium.getName(), checkSeat.getId(), checkSeat.getRow() + checkSeat.getColumn());

    }
}
