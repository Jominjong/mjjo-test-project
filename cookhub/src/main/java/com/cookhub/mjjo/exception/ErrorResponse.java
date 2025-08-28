package com.cookhub.mjjo.exception;

import java.time.Instant;

/* 에러 응답의 통일된 스키마.

status: HTTP 상태코드

error: 에러명(“Bad Request”, “Forbidden”…)

message: 상세 메시지

path: 요청 URI

timestamp: 발생 시각(서버 기준)*/

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, Instant.now());
    }
}
