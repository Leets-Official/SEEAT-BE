package com.seeat.server.domain.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReviewUpdateRequest {

    /**
     * 수정할 리뷰 ID
     */
    @Schema(example = "1")
    @NotNull(message = "수정할 리뷰ID는 필수입니다.")
    private Long reviewId;

    /**
     * 평점 (1~5)
     */
    @NotNull(message = "평점은 필수입니다.")
    @Schema(example = "3.2")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
    private double rating;

    /**
     * 텍스트 후기 내용
     */
    @NotNull(message = "후기 내용은 필수입니다.")
    @Schema(example = "짜릿한 주행을 할 수 있던 자리 입니다!")
    private String content;

    /**
     * 해시태그 ID 목록 (필수)
     */
    @NotNull(message = "해시태그 목록은 필수입니다.")
    @Size(min = 1, message = "최소 하나 이상의 해시태그가 필요합니다.")
    @Schema(example = "[\"1\", \"2\",\"3\"]")
    private List<Long> hashtags;

    /**
     * 사진 목록 (선택)
     */
    @Schema(description = "수정할 리뷰 이미지들", type = "string", format = "binary")
    @Size(max = 5, message = "이미지는 최대 5개까지 가능합니다.")
    private List<MultipartFile> photos;


}
