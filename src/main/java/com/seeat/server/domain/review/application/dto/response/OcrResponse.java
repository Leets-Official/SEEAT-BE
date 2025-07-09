package com.seeat.server.domain.review.application.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * OCR 결과를 저장하기 위한 DTO(Data Transfer Object) 클래스.
 *
 * @param theater    극장명(예: CGV 야탑)
 * @param title      영화제목(예: F1 더 무비)
 * @param movieType  영화 상영 종류(예: 2D, 4DX, IMAX 등)
 * @param seat       좌석(예: H13)
 * @param hall       상영관(예: 2관 (Laser))
 */
@Builder
public record OcrResponse(
        String theater,
        String title,
        String movieType,
        String seat,
        String hall
) {

    /**
     * OCR API에서 추출한 텍스트 리스트를 OcrResponse로 변환하는 정적 팩토리 메서드.
     * strings: [titleInfo, hallInfo, seatInfo] 순서로 들어온다고 가정
     */
    public static OcrResponse from(List<String> strings) {
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

        return OcrResponse.builder()
                .theater(theater)
                .title(title)
                .movieType(movieType)
                .seat(seat)
                .hall(hall)
                .build();
    }
}
