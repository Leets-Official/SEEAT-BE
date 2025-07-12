package com.seeat.server.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtFailureHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // 로그인 필요 에러 발생
        CustomException exception = new CustomException(ErrorCode.INVALID_LOGIN, null);
        ApiResponse<Object> apiResponse = ApiResponse.fail(exception);

        // 응답 설정
        response.setStatus(apiResponse.httpStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON 응답
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
