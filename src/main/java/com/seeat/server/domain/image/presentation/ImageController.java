package com.seeat.server.domain.image.presentation;

import com.seeat.server.domain.image.application.dto.response.S3ImageResponse;
import com.seeat.server.domain.image.application.usecase.ImageUseCase;
import com.seeat.server.domain.image.presentation.swagger.ImageControllerSpec;
import com.seeat.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController implements ImageControllerSpec {

    private final ImageUseCase service;

    /**
     * S3 파일 업로드용 Presigned URL 발급
     * @param file   업로드할 파일 이미지
     */
    @GetMapping("/upload-url")
    public ApiResponse<?> getPresignedUploadUrls(
            @RequestParam List<String> file
    ) throws IOException {

        if (file.size() == 1) {
            S3ImageResponse response = service.getUploadPresignedURL(file.get(0));
            return ApiResponse.ok(response);
        } else {
            List<S3ImageResponse> responses = service.getUploadPresignedURL(file);
            return ApiResponse.ok(responses);
        }
    }
}

