package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import com.seeat.server.domain.review.domain.entity.Review;

import java.util.List;

/**
 * [해시태그 정의하는 인터페이스] 입니다
 * - 해시태그의 종류를 보여주는 기능을 정의하는 인터페이스 입니다.
 */
public interface HashTagUseCase {

    /// 조회
    List<HashTagResponse> loadAllHashTags();

    // 리뷰별 해시태그 조회
    List<List<String>> getHashTagsForReviews(List<Review> reviews);

}
