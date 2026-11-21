package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.exception.ApiException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Ops owns the email layout (D1): header, footer, fonts and width live in templates/email/*.html.
 * Core only supplies the body content.
 */
@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    public enum Template { STANDARD, TRANSACTIONAL, ANNOUNCEMENT }

    private final TemplateEngine engine;
    private final AppProperties props;
    private final HtmlSanitizer sanitizer;

    /** Resolves a template code; null/blank means STANDARD, an unknown code is a 400. */
    public Template resolve(String code) {
        if (code == null || code.isBlank()) return Template.STANDARD;
        try {
            return Template.valueOf(code.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("UNKNOWN_TEMPLATE", "Unknown templateCode: " + code);
        }
    }

    public String render(Template template, String subject, String content) {
        AppProperties.Brand brand = props.mail().brand();
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("subject", subject);
        ctx.setVariable("content", sanitizer.clean(content));
        ctx.setVariable("brand", brand);
        return engine.process("email/" + template.name().toLowerCase(Locale.ROOT), ctx);
    }
}
