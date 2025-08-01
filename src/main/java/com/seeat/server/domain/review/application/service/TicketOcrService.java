package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review.external.NaverOcrApi;
import com.seeat.server.domain.review.application.dto.response.OcrResponse;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
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

            /// OCR API 호출 (텍스트 추출 결과 리스트)
            List<String> result = naverOcrApi.callApi(HttpMethod.POST.name(), tempFile.getAbsolutePath(), ext);

            /// OCR 결과를 분기
            List<String> strings = toList(result);

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
    private List<String> toList(List<String> strings) {

        /// OCR 결과를 4가지로 분기
        if (strings == null || strings.size() < 3) {
            return null;
        }

        String titleInfo = strings.get(0);
        String hallInfo = strings.get(1);
        String seatInfo = strings.get(2);

        // 영화제목, 종류 추출 (공통)
        String[] titleParts = titleInfo.split("\n");
        String title = titleParts.length > 0 ? titleParts[0] : null;

        // 마지막 줄을 movieType으로
        String movieType = titleParts.length > 1 ? titleParts[titleParts.length - 1] : null;

        // CGV/메가박스 분기
        String theater = null;
        String hall = null;
        String seat = null;

        String[] hallLines = hallInfo.split("\n");
        String[] seatLines = seatInfo.split("\n");

        boolean isCgv = hallLines[0].equals("상영관");

        if (isCgv) {
            // CGV
            // hallLines: ["상영관", "CGV 야탑", "2관 (Laser)"]
            theater = hallLines.length > 1 ? hallLines[1] : null;
            hall = hallLines.length > 2 ? hallLines[2] : null;
            // 좌석: ["좌석", "일반 1", "H13"]
            seat = seatLines.length > 2 ? seatLines[2] : (seatLines.length > 1 ? seatLines[1] : null);
        } else {
            // 메가박스
            // hallLines: ["분당", "5관 [발코니] (4층)"]
            theater = hallLines[0];
            hall = hallLines.length > 1 ? hallLines[1] : null;
            // 좌석: ["좌석", "L4"]
            seat = seatLines.length > 1 ? seatLines[1] : null;
        }

        return List.of(theater, title, movieType, hall, seat);
    }


    /// DB에서 결과 가져오기
    private OcrResponse toResponse(List<String> strings) {
        if (strings == null || strings.size() < 3) {
            return null;
        }

        /// DB에서 해당 값들 조회
        String theater = strings.get(0);
        String title = strings.get(1);
        String movieType = strings.get(2);
        String hall = strings.get(3);
        String seat = strings.get(4);

        /// 체크
        Seat checkSeat = theaterService.getSeatByName(theater, hall, seat);
        Auditorium checkAuditorium = checkSeat.getAuditorium();
        Theater checkTheater = checkAuditorium.getTheater();

        /// DTO 변환
        return OcrResponse.from(checkTheater.getName(), title, movieType, checkAuditorium.getId(), checkAuditorium.getName(), checkSeat.getId(), checkSeat.getRow() + checkSeat.getColumn());

    }
}
