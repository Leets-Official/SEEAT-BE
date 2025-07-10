package com.seeat.server.global.util;
/**
 * JWT 관련 상수 정의 클래스
 *
 * 토큰 생성 및 검증, 쿠키와 헤더 처리 시 사용되는 키 및 타입을 관리합니다.
 */
public class JwtConstants {
    // JWT 클레임 키
    public static final String AUTHORITIES_KEY = "auth";
    public static final String USER_ID_KEY = "userId";

    // 헤더 관련
    public static final String TOKEN_TYPE = "Bearer";

    // 쿠키
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
}
