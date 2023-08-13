package com.c9pay.storeservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("토큰이 유효하지 않습니다.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(Throwable cause) {
        super("토큰이 유효하지 않습니다.", cause);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
