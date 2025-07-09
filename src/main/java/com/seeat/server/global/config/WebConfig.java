package com.seeat.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 전역 설정 클래스
 * Spring MVC 전반에 걸친 공통 설정을 담당하며,
 * CORS 설정, 인터셉터 등록, 리소스 핸들링 등을 구성한다.
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.front.local}")
    private String front_local;

    @Value("${cors.front.dev}")
    private String front_dev;

    /**
     * CORS 설정을 진행합니다.
     * @param registry Cors 등록을 위한 파라미터 입니다.
     */

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(front_local, front_dev)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
