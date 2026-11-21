package com.gps.os.service;

import com.gps.os.config.AppProperties;
import com.gps.os.dto.EmailDtos.*;
import com.gps.os.entity.Channel;
import com.gps.os.entity.NotificationLog;
import com.gps.os.exception.ApiException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Accepts an email from Core: checks limits, applies the template and mail rules, records it in
 * NotificationLog (idempotent on requestId) and hands it to the async dispatcher.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    public record PreparedAttachment(String fileName, String contentType, byte[] bytes) {}

    public record PreparedEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String html,
                                String text, List<PreparedAttachment> attachments) {}

    private final AppProperties props;
    private final EmailTemplateService templates;
    private final HtmlSanitizer sanitizer;
    private final NotificationLogService logs;
    private final EmailDispatcher dispatcher;

    public EmailAccepted accept(EmailRequest r) {
        List<String> to = nz(r.to());
        List<String> cc = nz(r.cc());
        List<String> bcc = nz(r.bcc());
        if (to.size() + cc.size() + bcc.size() > props.mail().maxRecipients()) {
            throw ApiException.badRequest("TOO_MANY_RECIPIENTS",
                    "At most " + props.mail().maxRecipients() + " recipients across to, cc and bcc");
        }
        if (r.subject().length() > props.mail().maxSubjectLength()) {
            throw ApiException.badRequest("SUBJECT_TOO_LONG", "Subject is longer than " + props.mail().maxSubjectLength());
        }
        EmailTemplateService.Template template = templates.resolve(r.templateCode()); // unknown code -> 400
        List<PreparedAttachment> attachments = decodeAttachments(r.attachments());

        var created = logs.createIfAbsent(r.requestId(), Channel.EMAIL, r.category(), r.userId(),
                String.join(",", to));
        NotificationLog log = created.log();
        if (!created.isNew()) {
            return new EmailAccepted(String.valueOf(log.getId()), log.getStatus()); // repeat: do not send again
        }
        String html = templates.render(template, r.subject(), r.content());
        dispatcher.dispatch(log.getId(), new PreparedEmail(to, cc, bcc, r.subject(), html, sanitizer.toText(html), attachments));
        return new EmailAccepted(String.valueOf(log.getId()), log.getStatus());
    }

    /** Used by Ops' own jobs (e.g. activation reminders). Goes through the same rules and logging. */
    public EmailAccepted sendInternal(String requestId, String templateCode, Long userId, String toEmail,
                                      String subject, String content, String category) {
        return accept(new EmailRequest(requestId == null ? UUID.randomUUID().toString() : requestId, templateCode,
                List.of(toEmail), List.of(), List.of(), subject, content, List.of(), category, userId));
    }

    private List<PreparedAttachment> decodeAttachments(List<Attachment> in) {
        List<PreparedAttachment> out = new ArrayList<>();
        long total = 0;
        for (Attachment a : nz(in)) {
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(a.base64().replaceAll("\\s", "")); // strict: no silent skipping
            } catch (IllegalArgumentException e) {
                throw ApiException.badRequest("ATTACHMENT_INVALID", "Attachment " + a.fileName() + " is not valid Base64");
            }
            total += bytes.length;
            if (total > props.mail().maxAttachmentBytes()) {
                throw ApiException.badRequest("ATTACHMENTS_TOO_LARGE",
                        "Attachments exceed " + props.mail().maxAttachmentBytes() / (1024 * 1024) + " MB in total");
            }
            out.add(new PreparedAttachment(a.fileName(), a.contentType(), bytes));
        }
        return out;
    }

    private static <T> List<T> nz(List<T> l) {
        return l == null ? List.of() : l;
    }
}
