package com.seeat.server.domain.review.application.usecase;

import com.seeat.server.domain.review.application.dto.response.HashTagResponse;
import java.util.List;

/**
 * [해시태그 정의하는 인터페이스] 입니다
 * - 해시태그의 종류를 보여주는 기능을 정의하는 인터페이스 입니다.
 */
public interface HashTagUseCase {

    /// 조회
    List<HashTagResponse> loadAllHashTags();


}
