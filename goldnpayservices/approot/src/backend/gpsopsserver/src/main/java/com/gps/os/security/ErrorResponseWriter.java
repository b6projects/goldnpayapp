package com.gps.os.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gps.os.exception.ProblemDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Writes RFC 7807 bodies from servlet filters, where the MVC advice does not apply. */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {
    private final ObjectMapper mapper;

    public void write(HttpServletRequest req, HttpServletResponse res, HttpStatus status, String code, String detail)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), ProblemDetails.of(status, code, detail, req));
    }
}
