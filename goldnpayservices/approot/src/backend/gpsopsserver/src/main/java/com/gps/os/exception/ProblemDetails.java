package com.gps.os.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemDetails {
    private ProblemDetails() {}

    public static ProblemDetail of(HttpStatus status, String errorCode, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("errorCode", errorCode);
        pd.setProperty("traceId", MDC.get("traceId"));
        return pd;
    }
}
