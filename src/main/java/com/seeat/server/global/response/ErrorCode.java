package com.seeat.server.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역에서 사용하는 에러 코드 Enum입니다.
 * 각 에러는 고유 코드, HTTP 상태, 메시지를 포함합니다.
 *
 * 0~999 : 공통 에러
 * 1000~1999 : 유저 관련 에러
 * 2000~2999 : 영화관 관련 에러
 * 3000~3999 : 리뷰 관련 에러
 * 10000 이상 : 기타 파라미터 등
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {


    // ========================
    // 0~1000 : 공통 및 보안 에러
    // ========================

    /** 테스트용 에러 */
    TEST_ERROR(100, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),

    // 400 Bad Request
    BAD_REQUEST(400_000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_FILE_FORMAT(400_001, HttpStatus.BAD_REQUEST, "업로드된 파일 형식이 올바르지 않습니다."),
    INVALID_INPUT(400_002, HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    NULL_VALUE(400_003, HttpStatus.BAD_REQUEST, "Null 값이 들어왔습니다."),

    // 401 Unauthorized
    TOKEN_EXPIRED(401_000, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(401_001, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND(401_002, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    TOKEN_UNSUPPORTED(401_003, HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰 형식입니다."),
    INVALID_CREDENTIALS(401_004, HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(401_005, HttpStatus.UNAUTHORIZED, "재발급 토큰이 유효하지 않습니다."),
    INVALID_ACCESS_TOKEN(401_006, HttpStatus.UNAUTHORIZED, "접근 토큰이 유효하지 않습니다."),
    INVALID_TOKEN(401_007, HttpStatus.UNAUTHORIZED, "토큰이 생성되지 않았습니다."),
    INVALID_LOGIN(401_008, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    REFRESH_TOKEN_NOT_FOUND(401_011, HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(401_009, HttpStatus.UNAUTHORIZED, "저장된 리프레시 토큰과 일치하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(401_010, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    UNSUPPORTED_SOCIAL_LOGIN(401_012, HttpStatus.UNAUTHORIZED, "지원하지 않는 소셜 로그인 방식입니다."),


    // 403 Forbidden
    FORBIDDEN(403_000, HttpStatus.FORBIDDEN, "접속 권한이 없습니다."),
    ACCESS_DENY(403_001, HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    INVALID_ENVIRONMENT(403_002, HttpStatus.FORBIDDEN, "개발 환경에서만 사용할 수 있는 기능입니다."),

    /** 엔드포인트 없음  */
    NOT_FOUND_END_POINT(404, HttpStatus.NOT_FOUND, "요청한 엔드포인트가 존재하지 않습니다."),

    /** 서버 내부 오류  */
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    /** 요청 파라미터 오류 */
    BAD_PARAMETER(999, HttpStatus.BAD_REQUEST, "요청 파라미터에 문제가 존재합니다."),

    // ========================
    // 1000~1999 : 유저 관련 응답 에러
    // ========================

    /** 유저 없음 */
    NOT_USER(1000, HttpStatus.NOT_FOUND, "해당하는 유저가 존재하지 않습니다."),
    NOT_TEMP_USER(1001, HttpStatus.NOT_FOUND, "임시 유저가 존재하지 않습니다."),

    /** 최초 로그인 추가 정보 필요 */
    FIRST_LOGIN(1200, HttpStatus.NOT_FOUND, "최초 로그인유저이기에 추가정보기입이 필요합니다."),

    // ========================
    // 2000~2999 : 영화관 관련 에러
    // ========================
    NOT_THEATER(2000, HttpStatus.NOT_FOUND, "해당 ID를 가진 영화관이 존재하지 않습니다."),
    NOT_AUDITORIUM(2000, HttpStatus.NOT_FOUND, "해당 ID를 가진 상영관이 존재하지 않습니다."),
    NOT_SEAT(2000, HttpStatus.NOT_FOUND, "해당 ID를 가진 좌석이 존재하지 않습니다."),

    // ========================
    // 3000~3999 : 리뷰 관련 에러
    // ========================
    NOT_REVIEW(3000, HttpStatus.NOT_FOUND, "해당하는 리뷰가 존재하지 않습니다."),
    INVALID_HASHTAG(3001, HttpStatus.BAD_REQUEST, "모든 해시태그 항목(음향, 동반인, 관람환경)을 최소 1개 이상 작성해야 합니다.");



    /** 에러 코드 (고유값) */
    private final Integer code;

    /** HTTP 상태 코드 */
    private final HttpStatus httpStatus;

    /** 에러 메시지 */
    private final String message;



    /**
     * 메시지를 바탕으로 ErrorCode를 반환합니다.
     * 동일한 메시지가 여러 ErrorCode에 할당된 경우, 첫 번째로 일치하는 ErrorCode를 반환합니다.
     *
     * @param message 에러 메시지
     * @return 일치하는 ErrorCode
     * @throws IllegalArgumentException 메시지에 해당하는 ErrorCode가 없을 때
     */
    public static ErrorCode fromMessage(String message) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getMessage().equals(message)) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("해당 message를 가진 ErrorCode가 존재하지 않습니다: " + message);
    }
}
