package com.gps.os.exception;

import org.springframework.http.HttpStatus;

/** Domain exception rendered as an RFC 7807 ProblemDetail by {@link GlobalExceptionHandler}. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() { return status; }
    public String getErrorCode() { return errorCode; }

    public static ApiException notFound(String what) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", what + " not found");
    }
    public static ApiException badRequest(String code, String msg) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, msg);
    }
    public static ApiException unavailable(String code, String msg) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, code, msg);
    }
}
