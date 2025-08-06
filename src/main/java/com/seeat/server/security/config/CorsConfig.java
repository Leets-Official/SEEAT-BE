package com.seeat.server.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${cors.front.local}")
    private String front_local;

    @Value("${cors.front.dev}")
    private String front_dev;

    @Value("${cors.back.dev}")
    private String back_dev;

    /**
     * CORS 설정을 진행합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        /// CORS 추가
        configuration.addAllowedOriginPattern(front_local);
        configuration.addAllowedOriginPattern(front_dev);
        configuration.addAllowedOriginPattern(back_dev);

        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        configuration.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
