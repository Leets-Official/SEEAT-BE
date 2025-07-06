package com.seeat.server.domain.review.presentation.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * [좌석 후기 작성]을 위한 요청 DTO
 *
 * @param seatId        좌석 번호 (예: "특정 영화관의 F12 아이디")
 * @param movieTitle    영화 ID
 * @param rating        평점 (1~5)
 * @param content       텍스트 후기
 * @param hashtags      해시태그 ID 목록 (필수)
 * @param photos        사진 목록 (선택)
 */

@Builder
public record ReviewRequest(
        Long seatId,
        String movieTitle,
        int rating,
        String content,
        List<Long> hashtags,
        List<MultipartFile> photos
) {}
