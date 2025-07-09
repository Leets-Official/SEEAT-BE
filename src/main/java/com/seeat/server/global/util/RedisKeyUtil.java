package com.seeat.server.global.util;

import java.util.UUID;

/**
 * Redis 키 관리 유틸리티 클래스
 *
 * Redis에서 사용되는 키들을 중앙에서 관리합니다.
 */
public class RedisKeyUtil {
    // OAuth2 관련 키
    private static final String OAUTH2_TEMP_USER = "OAUTH2_TEMP_USER:";

    public static String generateOAuth2TempUserKey() {
        return OAUTH2_TEMP_USER + UUID.randomUUID();
    }
}
