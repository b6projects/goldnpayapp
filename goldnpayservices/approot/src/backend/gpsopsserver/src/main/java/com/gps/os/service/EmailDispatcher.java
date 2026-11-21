package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.service.EmailService.PreparedEmail;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends emails asynchronously. The From and reply-to addresses come from Ops config only.
 * On failure it retries with exponential back-off, then marks the NotificationLog row FAILED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDispatcher {
    private final JavaMailSender mailSender;
    private final AppProperties props;
    private final NotificationLogService logs;

    @Async
    public void dispatch(Long logId, PreparedEmail email) {
        int maxAttempts = 1 + props.mail().retries();
        String lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logs.markAttempt(logId, attempt);
            try {
                mailSender.send(build(email));
                logs.markSent(logId, attempt);
                return;
            } catch (MailParseException | IllegalArgumentException e) {
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.warn("Email {} rejected permanently: {}", logId, e.getClass().getSimpleName());
                logs.markFailed(logId, attempt, lastError);
                return; // retrying cannot help
            } catch (Exception e) {
                lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                log.warn("Email {} attempt {}/{} failed: {}", logId, attempt, maxAttempts, e.getClass().getSimpleName());
                if (attempt < maxAttempts) sleep(props.mail().backoffMs() * (1L << (attempt - 1)));
            }
        }
        logs.markFailed(logId, maxAttempts, lastError);
    }

    private MimeMessage build(PreparedEmail e) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
        h.setFrom(new InternetAddress(props.mail().from(), props.mail().brand().name(), StandardCharsets.UTF_8.name()));
        if (props.mail().replyTo() != null && !props.mail().replyTo().isBlank()) h.setReplyTo(props.mail().replyTo());
        h.setTo(e.to().toArray(String[]::new));
        if (!e.cc().isEmpty()) h.setCc(e.cc().toArray(String[]::new));
        if (!e.bcc().isEmpty()) h.setBcc(e.bcc().toArray(String[]::new));
        h.setSubject(e.subject());
        h.setText(e.text(), e.html());
        for (var a : e.attachments()) {
            h.addAttachment(a.fileName(), new ByteArrayDataSource(a.bytes(), a.contentType()));
        }
        return msg;
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
