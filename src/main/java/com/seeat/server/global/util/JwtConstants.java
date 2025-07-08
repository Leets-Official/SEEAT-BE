package com.seeat.server.global.util;

public class JwtConstants {
    // JWT 클레임 키
    public static final String AUTHORITIES_KEY = "auth";
    public static final String USER_ID_KEY = "userId";

    // 헤더 관련
    public static final String TOKEN_TYPE = "Bearer";

    // 쿠키
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
}
