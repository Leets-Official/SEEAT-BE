package com.seeat.server.domain.image.application.dto.response;

import lombok.Builder;

/**
 * S3 이미지 DTO
 * @param fileName      원본 파일 이름
 * @param preSignedUrl  저장할 S3 주소
 */
@Builder
public record S3ImageResponse(
        String fileName,
        String preSignedUrl) {

    /// 정적 팩토리 메서드
    public static S3ImageResponse from(String fileName, String preSignedUrl) {
        return S3ImageResponse.builder()
                .fileName(fileName)
                .preSignedUrl(preSignedUrl)
                .build();
    }
}
