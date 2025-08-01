package com.seeat.server.domain.image.presentation.swagger;

import com.seeat.server.domain.image.application.dto.response.S3ImageResponse;
import com.seeat.server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Tag(name = "이미지 API", description = "S3 PreSignedURL 요청하는 API 입니다.")
public interface ImageControllerSpec {

    /**
     * 파일 이미지 업로드를 수행할 S3 주소를 요청
     *
     * @param file 이미지 이름
     */
    @Operation(
            summary = "S3 Presigned URL 발급",
            description = "S3에 이미지 업로드를 위한 Presigned URL을 발급합니다. 하나 또는 여러 개의 파일명을 query param으로 전달하세요."
    )
    ApiResponse<?> getPresignedUploadUrls(
            @Parameter(
                    description = "업로드할 파일 이름 목록",
                    example = "[\"image1.jpg\", \"image2.png\"]"
            )
            @RequestParam List<String> file
    ) throws IOException;



}
