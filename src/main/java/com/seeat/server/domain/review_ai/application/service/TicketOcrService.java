package com.seeat.server.domain.review_ai.application.service;

import com.seeat.server.domain.review_ai.application.usecase.TicketOcrUseCase;
import com.seeat.server.domain.review_ai.external.NaverOcrApi;
import com.seeat.server.domain.review_ai.application.dto.response.OcrResponse;
import com.seeat.server.domain.theater.application.usecase.TheaterUseCase;
import com.seeat.server.domain.theater.domain.entity.Auditorium;
import com.seeat.server.domain.theater.domain.entity.Seat;
import com.seeat.server.domain.theater.domain.entity.Theater;
import com.seeat.server.global.response.ErrorCode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
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

        /// 임시 파일로 저장
        File tempFile = null;
        try {
            tempFile = File.createTempFile("ocr_", "." + ext);
            file.transferTo(tempFile);

            /// OCR API 호출 (텍스트 추출 결과 리스트)
            List<String> result = naverOcrApi.callApi(HttpMethod.POST.name(), tempFile.getAbsolutePath(), ext);

            /// OCR 결과를 분기
            OcrCGVResponse cgvResponse = cgvImageOCR(result);

            /// 분기한 것을 DTO로 변환
            return toResponse(cgvResponse);

        } finally {
            // 임시 파일 삭제 (예외 발생 여부와 관계없이 항상 삭제)
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    ///
    private OcrCGVResponse cgvImageOCR(List<String> strings) {

        /// 예외 처리
        if (strings == null || strings.size() < 3) {
            return null;
        }

        String titleInfo = strings.get(0);
        String hallInfo = strings.get(1);
        String seatInfo = strings.get(2);

        /// 영화제목 정보 추출
        String[] titleParts = titleInfo.split("\\(");
        String title = titleParts.length > 0 ? titleParts[0] : null;

        /// 상영관 정보 추출
        String[] hallLines = hallInfo.split("\n");
        String rawTheater = hallLines.length > 0 ? hallLines[0].trim() : null;
        String theater = null;

        /// CGV가 없다면 추가
        if (rawTheater != null) {
            theater = rawTheater.startsWith("CGV") ? rawTheater : "CGV " + rawTheater;
        }


        /// "IMAX관 7층" 같은 줄에서 "IMAX관"만 추출
        String hallLine = hallLines.length > 1 ? hallLines[1] : null;
        String hall = null;
        if (hallLine != null) {
            hall = hallLine.split(" ")[0];
        }

        /// 좌석 정보 추출
        String[] seatLines = seatInfo.split("\n");
        List<String> seat = new ArrayList<>();

        /// 콤마로 되어있는 것 나누기
        for (String line : seatLines) {
            String[] parts = line.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    seat.add(trimmed);
                }
            }
        }


        return OcrCGVResponse.from(theater, title, hall, seat);
    }



    /// DB에서 결과 가져오기
    private OcrResponse toResponse(OcrCGVResponse cgvResponse) {

        /// 예외처리
        if (cgvResponse == null) {
            return null;
        }

        /// DB에서 해당 값들 조회
        String theater = cgvResponse.theater;
        String title = cgvResponse.title;
        String auditorium = cgvResponse.auditorium;
        List<String> seats = cgvResponse.seats;

        /// 체크
        List<Seat> checkSeats = theaterService.getSeatByName(theater, auditorium, seats);

        /// 2개의 티켓의 상영관은 동일하기에,
        Seat checkSeat = checkSeats.get(0);

        Auditorium checkAuditorium = checkSeat.getAuditorium();
        Theater checkTheater = checkAuditorium.getTheater();

        /// DTO 변환
        return OcrResponse.from(checkTheater.getName(), title, checkAuditorium.getId(), checkAuditorium.getName(), checkSeats);

    }

    /// CGV OCR 응답을 내부에서 사용하는 DTO
    @Builder
    record OcrCGVResponse(
            String theater,
            String title,
            String auditorium,
            List<String> seats){
        /// 정적 팩토리 메서드
        public static OcrCGVResponse from(String theater, String title, String auditorium, List<String> seats) {
            return OcrCGVResponse.builder()
                    .theater(theater)
                    .title(title)
                    .auditorium(auditorium)
                    .seats(seats)
                    .build();
        }
    }

}
