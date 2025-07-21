package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
/**
 * 리뷰 이미지를 관리하는 인터페이스 입니다.
 * - 이미지 저장 / 조회 / 삭제의 기능을 수행합니다.
 */
public interface ReviewImageUseCase {

    /// 하나의 사진 저장하기
    String saveReviewImage(Review review, MultipartFile photo) throws IOException;

    /// 여러개의 사진 저장
    List<String> saveReviewImage(Review review, List<MultipartFile> photos) throws IOException;

    /// 리뷰에 따른 이미지 목록 조회
    List<ReviewImage> getReviewImagesByReview(Review review);

    /// 삭제
    void deleteReviewImage(String fileName);



}
