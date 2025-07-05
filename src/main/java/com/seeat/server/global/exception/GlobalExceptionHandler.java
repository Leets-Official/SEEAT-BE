package com.seeat.server.global.exception;

import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import com.seeat.server.global.response.FieldErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /// 공통 처리 메서드
    private ApiResponse<CustomException> handleCustomException(CustomException customException) {

        return ApiResponse.fail(customException);
    }

    /// 예외 처리
    // 최하위 예외처리
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<CustomException> handleException(Exception e) {

        /// 로그 발생
        log.error(e.getMessage(), e);

        /// 500 예외 코드 검색
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        /// 해당 예외 코드로 예외 처리
        CustomException exception = new CustomException(errorCode, null);

        return handleCustomException(exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            IllegalStateException.class, IllegalArgumentException.class})
    public ApiResponse<CustomException> handleIllegalStateException(Exception e) {

        /// 메세지 바탕으로 예외 코드 검색
        ErrorCode errorCode = ErrorCode.fromMessage(e.getMessage());

        /// 해당 예외 코드로 예외 처리
        CustomException exception = new CustomException(errorCode, null);

        return handleCustomException(exception);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<CustomException> handleValidationExceptions(MethodArgumentNotValidException e) {

        /// 파라미터용 예외 코드
        ErrorCode errorCode = ErrorCode.BAD_PARAMETER;

        /// BindingResult 바탕으로 필드에러 List 생성
        List<FieldErrorResponse> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> FieldErrorResponse.of(error.getField(), error.getDefaultMessage()))
                .toList();

        CustomException exception = new CustomException(errorCode, errors);

        return handleCustomException(exception);
    }

}
