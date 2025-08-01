package com.seeat.server.domain.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@Schema(name = "[요청][리뷰] 리뷰 수정 Request",description = "리뷰 수정에 대한 DTO 입니다.")
public class ReviewUpdateRequest {

    /**
     * 평점 (1~5)
     */
    @Schema(example = "3.2")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
    private double rating;

    /**
     * 텍스트 후기 내용
     */
    @Schema(example = "짜릿한 주행을 할 수 있던 자리 입니다!")
    private String content;

    /**
     * 해시태그 ID 목록 (필수)
     */
    @Schema(example = "[\"1\", \"2\",\"3\"]")
    private List<Long> hashtags;

    /**
     * 사진 목록 (선택)
     */
    @Schema(description = "수정할 리뷰 이미지들")
    @Size(max = 5, message = "이미지는 최대 5개까지 가능합니다.")
    private List<String> images;


}
