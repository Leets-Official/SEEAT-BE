package com.seeat.server.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component

/**
 * HTTP 요청에 대한 로그를 기록한다.
 */

@Order(Integer.MIN_VALUE)
public class LogFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous";
        String httpMethod = request.getMethod();
        String uri = URLDecoder.decode(request.getRequestURI(), StandardCharsets.UTF_8);

        // Origin 헤더 가져오기
        String origin = request.getHeader("Origin");

        log.info("[HTTP 요청 로깅]: [{}] {} - 사용자: {} - Origin: {}", httpMethod, uri, username, origin);

        filterChain.doFilter(request, response);
    }
}
