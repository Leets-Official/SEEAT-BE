package com.seeat.server.domain.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;

    /**
     * 영화 제목 또는 영화 ID
     */
    @NotBlank(message = "영화 제목은 필수입니다.")
    private String movieTitle;

    /**
     * 평점 (1~5)
     */
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
    private int rating;

    /**
     * 텍스트 후기 내용
     */
    @NotNull(message = "후기 내용은 필수입니다.")
    private String content;

    /**
     * 해시태그 ID 목록 (필수)
     */
    @NotNull(message = "해시태그 목록은 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 해시태그가 필요합니다.")
    private List<Long> hashtags;

    /**
     * 사진 목록 (선택)
     */
    @Schema(description = "리뷰 이미지들", type = "string", format = "binary")
    private List<MultipartFile> photos;
}
