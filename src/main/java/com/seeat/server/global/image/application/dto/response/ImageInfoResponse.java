package com.seeat.server.global.image.application.dto.response;

import com.seeat.server.domain.review.domain.entity.ReviewImage;
import lombok.Builder;
import java.util.List;
/**
 * 이미지 정보 DTO
 * @param imageUrl  이미지 url
 * @param order     이미지 순서
 */
@Builder
public record ImageInfoResponse(
        String imageUrl,
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
