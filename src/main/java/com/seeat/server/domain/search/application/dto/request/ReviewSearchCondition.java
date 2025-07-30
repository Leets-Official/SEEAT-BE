package com.seeat.server.domain.search.application.dto.request;
import com.seeat.server.domain.search.domain.entity.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 리뷰 검색 필터 요청을 위한 DTO
 * 리뷰 검색시 검색어, 상영관, 해시태그, 정렬기준으로 필터링합니다.
 *
 */
@Data
@Builder
@Schema(name = "[요청][검색] 리뷰 검색 필터 Request",description = "리뷰 검색 필터 요청에 대한 DTO 입니다.")
public class ReviewSearchCondition {
    @Schema(description = "검색어", example = "F1")
    private String keyword;           // 리뷰 내용, 영화 제목 등에 사용
    @Schema(description = "상영관 Id", example = "123")
    private String auditoriumId;        // 상영관 필터링
    @Schema(description = "해시태그 Id",example = "[\"1\", \"2\"]")
    private List<Long> hashTagIds;    // 해시태그로 필터링
    @Schema(description = "정렬기준(인기순, 최신순, 평점순)", example = "POPULAR")
    private SortType sort;              // 최신순, 평점순 등
}

