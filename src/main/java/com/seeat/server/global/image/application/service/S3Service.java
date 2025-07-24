package com.seeat.server.global.image.application.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.seeat.server.global.image.application.usecase.ImageUseCase;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AWS S3와의 연동을 통해 이미지 파일의 업로드 및 삭제를 담당합니다.
 * -단일 및 다중 파일 업로드, 파일 삭제 기능을 제공합니다
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service implements ImageUseCase {

    @Value("${cloud.aws.S3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final AmazonS3 s3Client;

    /// 저장하기

    /**
     * 하나의 파일 저장
     * @param file  저장할 이미지 파일
     */
    public String uploadFile(MultipartFile file) throws IOException {

        /// 예외처리
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException(ErrorCode.NOT_IMAGE.getMessage());
        }

        /// 파일 임시 저장
        File fileObj = convertMultiPartFileToFile(file);
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID() + "." + extension;

        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();

        return s3Client
                .getUrl(bucketName, fileName).toString();
    }

    /**
     * 한번에 여러 파일 저장하기
     * @param files 이미지들
     */
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {

        /// files 갯수 0 이면 반환 ""
        if(files == null || files.isEmpty()){
            throw new IllegalArgumentException(ErrorCode.NOT_IMAGE.getMessage());
        }

        /// 리스트 생성
        List<String> images = new ArrayList<>();

        /// 리스트에 추가하기
        for (int i = 0; i < files.size(); i++) {
            images.add(uploadFile(files.get(i)));
        }
        return images;
    }

    /// 삭제하기

    /**
     * 이미지 삭제하기
     * @param fileName  S3에서 삭제할 이미지 이름
     */
    public void deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
    }

    // 쓰지 말자! File 객체 생성됨!
    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new IOException(ErrorCode.INTERNAL_FILE_ERROR.getMessage());
        }
        return convertedFile;
    }

    private static String getFileExtension(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    }

}

