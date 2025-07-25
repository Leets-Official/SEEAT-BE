package com.seeat.server.global.image.application.usecase;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * [이미지 저장을 위한 유즈 케이스를 정의한 인터페이스]입니다.
 * - S3에서 다른 의존성을 참조할 때 서비스를 바꾸면 코드의 변동없이 유지보수 할 수 있습니다.
 */
public interface ImageUseCase {

    /// 저장
    // 한 장 저장하기
    String uploadFile(MultipartFile file) throws IOException;

    // 여러 장 저장하기
    List<String> uploadFiles(List<MultipartFile> files) throws IOException;

    /// 삭제
    // 한 장 삭제하기
    void deleteFile(String fileName);

    // 여러 장 삭제하기
    void deleteFile(List<String> fileNames);
}
