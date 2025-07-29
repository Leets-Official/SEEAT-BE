package com.seeat.server.domain.image.application.dto.response;

import com.seeat.server.domain.review.domain.entity.ReviewImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;
/**
 * 이미지 정보 DTO
 * @param imageUrl  이미지 url
 * @param order     이미지 순서
 */
@Builder
@Schema(name = "[응답][리뷰] 이미지 정보 Response", description = "리뷰에 포함된 이미지의 URL과 순서를 나타내는 DTO입니다.")
public record ImageInfoResponse(
        @Schema(description = "이미지 URL", example = "https://example.com/images/review1.jpg")
        String imageUrl,

        @Schema(description = "이미지 순서", example = "1")
        int order
) {

    /// 정적 팩토리 메서드
    public static ImageInfoResponse from(ReviewImage reviewImage) {
        return ImageInfoResponse.builder()
                .imageUrl(reviewImage.getImageUrl())
                .order(reviewImage.getDisplayOrder())
                .build();
    }

    /// 정적 팩토리 메서드
    public static List<ImageInfoResponse> from(List<ReviewImage> images) {
        return images.stream()
                .map(ImageInfoResponse::from)
                .toList();
    }
}
