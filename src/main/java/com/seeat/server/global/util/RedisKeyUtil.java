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
    private static final String SEPARATOR = ":";
    private static final String LIST = "list";
    private static final String BEST = "best";
    private static final String AUDITORIUM = "auditorium";
    private static final String REVIEW = "review";
    private static final String SUMMARY = "Summary";


    public static final String BEST_AUDITORIUM_LIST_KEY = BEST + SEPARATOR + AUDITORIUM + SEPARATOR + LIST;
    public static final String BEST_REVIEW_LIST_KEY = BEST + SEPARATOR + REVIEW + SEPARATOR + LIST;
    public static final String REVIEW_SUMMARY_KEY = REVIEW + SUMMARY;
    public static String generateOAuth2TempUserKey() {
        return OAUTH2_TEMP_USER + UUID.randomUUID();
    }
}
