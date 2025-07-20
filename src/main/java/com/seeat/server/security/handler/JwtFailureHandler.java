package com.seeat.server.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFailureHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        /// 예외코드 작성
        ErrorCode errorCode;

        /// 인증되지 않은 사용자가 접속할 때,
        if (authException instanceof InsufficientAuthenticationException){
            errorCode = ErrorCode.INVALID_LOGIN;
        } else {
            /// 예외 상황에 맞는 예외코드 발생
            errorCode= ErrorCode.fromMessage(authException.getMessage());
        }


        // 로그인 필요 에러 발생
        CustomException exception = new CustomException(errorCode, null);
        ApiResponse<Object> apiResponse = ApiResponse.fail(exception);

        /// 로그 남기기
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        log.info("[필터 EXCEPTION] 사용자: {}, 메서드: {}, URI: {}, 예외: {}", username, request.getMethod(), request.getRequestURI(), errorCode.getMessage());

        // 응답 설정
        response.setStatus(apiResponse.httpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON 응답
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
