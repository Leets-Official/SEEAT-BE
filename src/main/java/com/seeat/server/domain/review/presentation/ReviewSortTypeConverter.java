package com.seeat.server.domain.review.presentation;

import com.seeat.server.domain.review.application.dto.request.ReviewSortType;
import com.seeat.server.global.response.ErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 파라미터 변환을 위한 컨버터입니다.
 */
@Component
public class ReviewSortTypeConverter implements Converter<String, ReviewSortType> {

    /**
     * getCode를 바탕으로 enum를 인식하고, 대소문자 관계없이 비교해서 바꾸는 형식입니다.
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     */
    @Override
    public ReviewSortType convert(String source) {
        for (ReviewSortType type : ReviewSortType.values()) {
            if (type.getValue().equalsIgnoreCase(source)) {
                return type;
            }
        }
        throw new IllegalArgumentException(ErrorCode.BAD_PARAMETER.getMessage());
    }
}
