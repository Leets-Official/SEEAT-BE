package com.seeat.server.domain.auth.application.dto.response;

/**
 * accessToken 응답입니다.
 *
 * @param accessToken accessToken
 */
public record TokenResponse(
        String accessToken
) {
}
