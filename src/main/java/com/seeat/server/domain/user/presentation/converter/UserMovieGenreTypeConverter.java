package com.seeat.server.domain.user.presentation.converter;

import com.seeat.server.domain.theater.domain.entity.MovieGenre;
import com.seeat.server.global.response.ErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 파라미터 변환을 위한 컨버터입니다.
 * 영어(enum name) 또는 한글(displayName) 기반으로 MovieGenre 변환
 */
@Component
public class UserMovieGenreTypeConverter implements Converter<String, MovieGenre> {

    @Override
    public MovieGenre convert(String source) {
        for (MovieGenre genre : MovieGenre.values()) {
            // 영어 (enum name) 또는 한글 (displayName) 모두 허용
            if (genre.name().equalsIgnoreCase(source) || genre.getDisplayName().equalsIgnoreCase(source)) {
                return genre;
            }
        }

        throw new IllegalArgumentException(ErrorCode.BAD_PARAMETER.getMessage());
    }
}
