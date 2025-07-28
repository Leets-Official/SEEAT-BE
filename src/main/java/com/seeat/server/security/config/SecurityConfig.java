package com.seeat.server.security.config;

import com.seeat.server.security.handler.CustomOAuth2SuccessHandler;
import com.seeat.server.security.handler.JwtDeniedHandler;
import com.seeat.server.security.handler.JwtFailureHandler;
import com.seeat.server.security.jwt.JwtFilter;
import com.seeat.server.security.oauth2.application.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.seeat.server.domain.user.domain.entity.UserRole.ADMIN;
import static com.seeat.server.domain.user.domain.entity.UserRole.USER;

/**
 * Spring Security 설정 클래스
 *
 * 애플리케이션의 인증 및 권한 부여 정책을 정의한다.
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final JwtFilter jwtFilter;
    private final JwtFailureHandler jwtFailureHandler;
    private final JwtDeniedHandler jwtDeniedHandler;
    private final RequestMatcherHolder requestMatcherHolder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(requestMatcherHolder.getRequestMatchersByMinRole(null))
                        .permitAll()
                        .requestMatchers(requestMatcherHolder.getRequestMatchersByMinRole(USER))
                        .hasAnyAuthority(ADMIN.getRole(), USER.getRole())
                        .requestMatchers(requestMatcherHolder.getRequestMatchersByMinRole(ADMIN))
                        .hasAnyAuthority(ADMIN.getRole())
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            response.sendRedirect("/login?error");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    exception.authenticationEntryPoint(jwtFailureHandler)
                            .accessDeniedHandler(jwtDeniedHandler);
                });


        return http.build();
    }
}
