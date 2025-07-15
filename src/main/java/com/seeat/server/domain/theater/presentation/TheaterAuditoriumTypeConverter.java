package com.seeat.server.domain.theater.presentation;

import com.seeat.server.domain.theater.domain.entity.AuditoriumType;
import com.seeat.server.global.response.ErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 파라미터 변환을 위한 컨버터입니다.
 */
@Component
public class TheaterAuditoriumTypeConverter implements Converter<String, AuditoriumType> {

    /**
     * getCode를 바탕으로 4DX를 인식하고, 대소문자 관계없이 비교해서 바꾸는 형식입니다.
     * @param source the source object to convert, which must be an instance of {@code S} (never {@code null})
     */
    @Override
    public AuditoriumType convert(String source) {
        for (AuditoriumType type : AuditoriumType.values()) {
            if (type.getCode().equalsIgnoreCase(source)) {
                return type;
            }
        }
        throw new IllegalArgumentException(ErrorCode.BAD_PARAMETER.getMessage());
    }
}
