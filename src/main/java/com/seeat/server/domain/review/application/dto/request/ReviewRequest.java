package com.seeat.server.domain.review.application.dto.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 좌석 후기 작성 요청을 위한 DTO
 * 클라이언트가 좌석 후기 작성 시 필요한 정보를 담아 서버로 전달할 때 사용합니다.
 */

@Data
@Builder
public class ReviewRequest {

    /**
     * 좌석 ID (예: 특정 영화관의 F12 아이디)
     */
    private Long seatId;

    /**
     * 영화 제목 또는 영화 ID
     */
    private String movieTitle;

    /**
     * 평점 (1~5)
     */
    private int rating;

    /**
     * 텍스트 후기 내용
     */
    private String content;

    /**
     * 해시태그 ID 목록 (필수)
     */
    private List<Long> hashtags;

    /**
     * 사진 목록 (선택)
     */
    private List<MultipartFile> photos;
}
