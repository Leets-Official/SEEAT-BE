package com.seeat.server.domain.image.application.usecase;

import com.seeat.server.domain.image.application.dto.response.S3ImageResponse;

import java.io.IOException;
import java.util.List;

/**
 * [이미지 저장을 위한 유즈 케이스를 정의한 인터페이스]입니다.
 * - S3에서 다른 의존성을 참조할 때 서비스를 바꾸면 코드의 변동없이 유지보수 할 수 있습니다.
 * - Pre-singedURL 을 바탕으로 이미지 기능을 구현합니다.
 * - Pre-singedURL 을 발급 받는 로직이다
 */
public interface ImageUseCase {

    /// 파일 저장을 위한 presignedURL 제공
    S3ImageResponse getUploadPresignedURL(String file) throws IOException;

    /// 파일 저장을 위한 presignedURL 제공
    List<S3ImageResponse> getUploadPresignedURL(List<String> files) throws IOException;

    /// 검증
    boolean isValidImageUrl(String url);

    /// 파일 삭제
    void deleteFile(String file) throws IOException;

    void deleteFile(List<String> files) throws IOException;

}
