package com.seeat.server.global.response.pageable;

import lombok.Builder;
import org.springframework.data.domain.Slice;

import java.util.List;

/**
 * 슬라이싱 객체를 DTO 변환
 * @param content   내용
 * @param hasNext   다음 여부
 * @param page      요청 페이지
 * @param size      요청 사이즈
 */
@Builder
public record SliceResponse<T>(
        List<T> content,
        boolean hasNext,
        int page,
        int size
) {
    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return SliceResponse.<T>builder()
                .content(slice.getContent())
                .hasNext(slice.hasNext())
                .page(slice.getNumber() + 1)
                .size(slice.getSize())
                .build();
    }
}
