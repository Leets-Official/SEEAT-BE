package com.seeat.server.domain.image.external;

import com.seeat.server.domain.image.application.dto.response.S3ImageResponse;
import com.seeat.server.domain.image.application.usecase.ImageUseCase;
import com.seeat.server.domain.image.util.ImageUtil;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * AWS S3와의 연동을 통해 PresignedURL 을 얻어오는 기능을 담당합니다.
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service implements ImageUseCase {

    @Value("${cloud.aws.S3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    /// PreSignedURL을 위한 s3Presigner
    private final S3Presigner s3Presigner;

    /// 삭제를 위한 S3Client
    private final S3Client s3Client;

    /**
     * S3에 하나의 파일 저장
     * @param file  저장할 이미지 파일 이름
     */
    @Override
    public S3ImageResponse getUploadPresignedURL(String file) throws IOException {

        /// 파일명 수정
        String key = ImageUtil.generateKey(file);

        /// 파일 DTO 생성
        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                req -> req.signatureDuration(Duration.ofMinutes(15)) /// 15분 유효기간
                        .putObjectRequest(
                                PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(key)
                                        .build()
                        )
        );
        String presignedUrl = presignedRequest.url().toString();
        return S3ImageResponse.from(file, presignedUrl);
    }

    /**
     * S3에 여러개의 파일 저장
     * @param files  저장할 이미지 파일들 이름
     */
    @Override
    public List<S3ImageResponse> getUploadPresignedURL(List<String> files) {
        return files.stream()
                .map(file -> {
                    try {
                        return getUploadPresignedURL(file);
                    } catch (IOException e) {
                        throw new IllegalStateException(ErrorCode.INTERNAL_S3_PresignedURL_ERROR.getMessage());
                    }
                })
                .toList();
    }


    /**
     * 이미지 삭제하기
     * @param fileName  S3에서 삭제할 이미지 이름
     */
    @Override
    public void deleteFile(String fileName) {

        /// 삭제할 오브젝트 생성
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();


        /// 삭제 요청
        s3Client.deleteObject(request);
    }

    /**
     * 이미지 삭제하기
     * @param fileNames  S3에서 삭제할 이미지 여러 개
     */
    @Override
    public void deleteFile(List<String> fileNames) {

        /// 반복해서 삭제
        for (String fileName : fileNames) {
            deleteFile(fileName);
        }
    }

    @Override
    public boolean isValidImageUrl(String url) {

        /// 비어있다면 실패
        if (url == null || url.isBlank()) {
            return false;
        }

        /// HTTPS 프로토콜 확인
        if (!url.startsWith("https://")) {
            return false;
        }

        /// S3 도메인 체크

        if (!url.startsWith(getS3Domain())) {
            return false;
        }

        /// 버킷명이 실제로 URL에 포함되어 있는지 확인
        if (!url.contains(bucketName)) {
            return false;
        }


        /// 허용된 확장자 체크 (.jpg, .png 등)
        String lowerUrl = url.toLowerCase();
        List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".webp");

        /// 확장자로 끝나는지 체크
        boolean hasValidExtension = allowedExtensions.stream()
                .anyMatch(lowerUrl::endsWith);

        if (!hasValidExtension) {
            return false;
        }

        /// 경로 우회 또는 특수문자 검사 (보안 위험 회피)
        if (url.contains("..") || url.contains(" ") || url.contains("\\")) {
            return false;
        }

        /// 파일명 길이 제한 (예: 5자 이상 255자 이하)
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        if (fileName.length() < 5 || fileName.length() > 255) {
            return false;
        }

        return true;
    }

    /// S3 도메인 얻는 로직
    private String getS3Domain() {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
    }


}

