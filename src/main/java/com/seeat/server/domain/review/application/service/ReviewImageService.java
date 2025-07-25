package com.seeat.server.domain.review.application.service;

import com.seeat.server.domain.review.application.usecase.ReviewImageUseCase;
import com.seeat.server.domain.review.domain.entity.Review;
import com.seeat.server.domain.review.domain.entity.ReviewImage;
import com.seeat.server.domain.review.domain.repository.ReviewImageRepository;
import com.seeat.server.global.image.application.usecase.ImageUseCase;
import com.seeat.server.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewImageService implements ReviewImageUseCase {

    private final ReviewImageRepository repository;

    /// S3와 같은 클라우드 이미지 저장 의존성
    private final ImageUseCase imageService;

    /// 저장하기
    /**
     * 이미지 한 장 저장하기
     * @param review    리뷰
     * @param photo     저장할 이미지 한 장
     */
    @Override
    public String saveReviewImage(Review review, MultipartFile photo) throws IOException {

        /// 이미지 클라우드에 저장
        String uploadFile = imageService.uploadFile(photo);

        /// 객체 생성
        var image = ReviewImage.of(review, uploadFile, 1);

        /// DB 저장하고, 이미지 주소 반납
        return repository.save(image)
                .getImageUrl();
    }

    /**
     * 이미지 여러 장 저장하기
     * @param review        리뷰
     * @param photos        저장할 이미지 여러 장
     */
    @Override
    public List<String> saveReviewImage(Review review, List<MultipartFile> photos) throws IOException {

        /// 이미지가 없음
        if (photos.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.NO_IMAGE_REVIEW.getMessage());
        }

        /// 최대 등록 이미지 수량 정하기
        if (photos.size() >= 6) {
            throw new IllegalArgumentException(ErrorCode.TOO_MANY_IMAGES.getMessage());
        }

        /// 이미지 클라우드에 저장
        List<String> uploadFiles = imageService.uploadFiles(photos);

        /// 순서대로 저장하기
        AtomicInteger cnt = new AtomicInteger(0);
        return uploadFiles.stream()
                .map(im -> ReviewImage.of(review, im, cnt.getAndIncrement()))
                .map(repository::save)
                .map(ReviewImage::getImageUrl)
                .toList();

    }

    /// 조회하기
    /**
     * 리뷰에 존재하는 이미지 순서대로 조회
     * @param review    리뷰
     */
    @Override
    public List<ReviewImage> getReviewImagesByReview(Review review) {

        /// 리뷰에 해당하는 이미지 주소 순서대로 가져오기
        List<ReviewImage> images = repository.findByReview(review);

        /// 순서대로 정렬한 내용 출력
        return images.stream()
                .sorted(Comparator
                        .comparing(ReviewImage::getDisplayOrder))
                .toList();
    }

    /// 삭제하기
    /**
     * 리뷰에 존재하는 모든 이미지 삭제
     * @param review    리뷰
     */
    @Override
    public void deleteReviewImage(Review review) {

        /// 리뷰에 해당하는 파일 이름 다 가져오기
        List<ReviewImage> images = repository.findByReview(review);

        /// 파일 이름만 추출하기
        List<String> imagesList = images.stream()
                .map(ReviewImage::getImageUrl)
                .toList();

        /// 클라우드에서 삭제하기
        imageService.deleteFile(imagesList);

        /// DB에서도 삭제하기
        repository.deleteAllByImageUrlIn(imagesList);

    }


}
