package com.seeat.server.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.TreeMap;

/**
 * Swagger / OpenAPI 설정 클래스
 * - HTTPS 설정으로 인해 @OpenAPIDefinition 추가
 * - 스웨거 내부 JWT 설정 추가
 */

@Configuration
@Profile("dev")
@OpenAPIDefinition(servers = {@Server(url = "https://api.seeat.site", description = "SEEAT 개발 서버")})
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat("JWT")
        );
        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("SEEAT Swagger")
                .description("SEEAT 스웨거입니다.")
                .version("1.0.0");
    }

    @Bean
    public OpenApiCustomizer removeGenericSchemas() {
        return openApi -> {
            openApi.getComponents().getSchemas().keySet().removeIf(name ->
                    name.contains("ApiResponse") || name.contains("SliceResponse")
            );
        };
    }
    /// 스키마 이름 기준 오름차순

    @Bean
    public OpenApiCustomizer sortSchemasAlphabetically() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }
}
