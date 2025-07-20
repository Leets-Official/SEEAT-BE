package com.seeat.server.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seeat.server.global.response.ApiResponse;
import com.seeat.server.global.response.CustomException;
import com.seeat.server.global.response.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        /// 예외 상황에 맞는 예외코드 발생
        ErrorCode errorCode = ErrorCode.fromMessage(accessDeniedException.getMessage());

        /// 예외 응답값 생성
        CustomException exception = new CustomException(errorCode, null);
        ApiResponse<Object> apiResponse = ApiResponse.fail(exception);

        /// response 제작
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // JSON 응답
        objectMapper.writeValue(response.getWriter(), apiResponse);

    }
}

