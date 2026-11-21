package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;

class EmailFlowTest extends IntegrationTestBase {
    @MockitoSpyBean JavaMailSender mailSender;

    @BeforeEach
    void clean() {
        reset(mailSender);
    }

    private Map<String, Object> request(String requestId) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("requestId", requestId);
        m.put("to", List.of("client@example.com"));
        m.put("subject", "Welcome to GoldNPay");
        m.put("content", "<p>Hi Priya,</p><p>Your account is ready.</p>");
        m.put("category", "WELCOME");
        m.put("userId", 1024);
        return m;
    }

    private ResultActions send(Object body, boolean withKey) throws Exception {
        var req = post("/cs/v1/email/send").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        if (withKey) req.header("X-API-KEY", KEY);
        return mvc.perform(req);
    }

    private MimeMessage[] awaitMessages(int n) {
        await().atMost(Duration.ofSeconds(10)).until(() -> GREEN_MAIL.getReceivedMessages().length >= n);
        return GREEN_MAIL.getReceivedMessages();
    }

    private static String html(Part p) throws Exception {
        if (p.isMimeType("text/html")) return (String) p.getContent();
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = html(mp.getBodyPart(i));
                if (s != null) return s;
            }
        }
        return null;
    }

    private static void attachments(Part p, List<String> out) throws Exception {
        if (Part.ATTACHMENT.equalsIgnoreCase(p.getDisposition())) out.add(((BodyPart) p).getFileName());
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) attachments(mp.getBodyPart(i), out);
        }
    }

    @Test
    void requires_the_api_key() throws Exception {
        send(request(UUID.randomUUID().toString()), false).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_API_KEY"));
        mvc.perform(post("/cs/v1/email/send").header("X-API-KEY", "wrong").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request("x")))).andExpect(status().isUnauthorized());
        // the Ops->Core key does not open this door
        mvc.perform(post("/cs/v1/email/send").header("X-API-KEY", "test-ops-to-core-key")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void sends_the_email_wrapped_in_the_default_template_and_reports_status() throws Exception {
        String rid = UUID.randomUUID().toString();
        String res = send(request(rid), true).andExpect(status().isAccepted())
                .andExpect(jsonPath("$.notificationId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(res).get("notificationId").asText();

        MimeMessage[] msgs = awaitMessages(1);
        MimeMessage m = msgs[msgs.length - 1];
        assertThat(m.getSubject()).isEqualTo("Welcome to GoldNPay");
        assertThat(m.getFrom()[0].toString()).contains("no-reply@goldnpay.test");
        assertThat(m.getReplyTo()[0].toString()).contains("support@goldnpay.test");
        String body = html(m);
        assertThat(body).contains("Your account is ready.")          // Core's content
                .contains("GoldNPay")                                // Ops header/branding
                .contains("support@goldnpay.in")                    // Ops footer
                .doesNotContain("Unsubscribe");                      // STANDARD has no unsubscribe link

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mvc.perform(get("/cs/v1/email/" + id).header("X-API-KEY", KEY))
                        .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SENT"))
                        .andExpect(jsonPath("$.attempts").value(1)));
    }

    @Test
    void template_choice_changes_the_layout() throws Exception {
        Map<String, Object> r = request(UUID.randomUUID().toString());
        r.put("templateCode", "ANNOUNCEMENT");
        send(r, true).andExpect(status().isAccepted());
        MimeMessage[] msgs = awaitMessages(1);
        assertThat(html(msgs[msgs.length - 1])).contains("Unsubscribe");
    }

    @Test
    void unknown_template_is_a_400() throws Exception {
        Map<String, Object> r = request(UUID.randomUUID().toString());
        r.put("templateCode", "NOPE");
        send(r, true).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("UNKNOWN_TEMPLATE"));
    }

    @Test
    void a_repeated_requestId_does_not_send_a_second_email() throws Exception {
        String rid = UUID.randomUUID().toString();
        int before = GREEN_MAIL.getReceivedMessages().length;
        String first = send(request(rid), true).andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        awaitMessages(before + 1);
        String second = send(request(rid), true).andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        Thread.sleep(500);
        assertThat(GREEN_MAIL.getReceivedMessages()).hasSize(before + 1);
        assertThat(json.readTree(second).get("notificationId")).isEqualTo(json.readTree(first).get("notificationId"));
    }

    @Test
    void dangerous_markup_is_removed_before_rendering() throws Exception {
        Map<String, Object> r = request(UUID.randomUUID().toString());
        r.put("content", "<p onclick=\"steal()\">Hello</p><script>alert(1)</script>"
                + "<form action=\"https://evil.example\"><input name=\"pw\"></form>"
                + "<a href=\"javascript:alert(2)\">click</a><img src=\"x\" onerror=\"alert(3)\">");
        send(r, true).andExpect(status().isAccepted());
        MimeMessage[] msgs = awaitMessages(1);
        String body = html(msgs[msgs.length - 1]);
        assertThat(body).contains("Hello").contains("click")
                .doesNotContain("<script").doesNotContain("alert(1)").doesNotContain("onclick").doesNotContain("onerror")
                .doesNotContain("<form").doesNotContain("evil.example").doesNotContain("javascript:");
    }

    @Test
    void enforces_sending_limits() throws Exception {
        Map<String, Object> many = request(UUID.randomUUID().toString());
        List<String> to = new ArrayList<>();
        for (int i = 0; i < 51; i++) to.add("u" + i + "@example.com");
        many.put("to", to);
        send(many, true).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("TOO_MANY_RECIPIENTS"));

        Map<String, Object> fifty = request(UUID.randomUUID().toString());
        fifty.put("to", to.subList(0, 50));
        send(fifty, true).andExpect(status().isAccepted());

        Map<String, Object> longSubject = request(UUID.randomUUID().toString());
        longSubject.put("subject", "x".repeat(201));
        send(longSubject, true).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));

        Map<String, Object> injected = request(UUID.randomUUID().toString());
        injected.put("subject", "Hi\r\nBcc: attacker@example.com");
        send(injected, true).andExpect(status().isBadRequest());

        Map<String, Object> big = request(UUID.randomUUID().toString());
        String chunk = Base64.getEncoder().encodeToString(new byte[6 * 1024 * 1024]);
        big.put("attachments", List.of(Map.of("fileName", "a.bin", "contentType", "application/octet-stream", "base64", chunk),
                Map.of("fileName", "b.bin", "contentType", "application/octet-stream", "base64", chunk)));
        send(big, true).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("ATTACHMENTS_TOO_LARGE"));
    }

    @Test
    void validation_errors_follow_the_problem_detail_format() throws Exception {
        Map<String, Object> r = request(UUID.randomUUID().toString());
        r.put("to", List.of());
        send(r, true).andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("to: must contain at least one address"))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.instance").value("/cs/v1/email/send"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void attachments_are_delivered() throws Exception {
        Map<String, Object> r = request(UUID.randomUUID().toString());
        r.put("attachments", List.of(Map.of("fileName", "receipt.pdf", "contentType", "application/pdf",
                "base64", Base64.getEncoder().encodeToString("%PDF-1.7 hi".getBytes()))));
        send(r, true).andExpect(status().isAccepted());
        MimeMessage[] msgs = awaitMessages(1);
        List<String> names = new ArrayList<>();
        attachments(msgs[msgs.length - 1], names);
        assertThat(names).contains("receipt.pdf");
    }

    @Test
    void a_transient_smtp_failure_is_retried() throws Exception {
        doThrow(new MailSendException("smtp down")).doThrow(new MailSendException("smtp down"))
                .doCallRealMethod().when(mailSender).send(any(MimeMessage.class));
        String res = send(request(UUID.randomUUID().toString()), true).andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(res).get("notificationId").asText();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mvc.perform(get("/cs/v1/email/" + id).header("X-API-KEY", KEY))
                        .andExpect(jsonPath("$.status").value("SENT")).andExpect(jsonPath("$.attempts").value(3)));
    }

    @Test
    void persistent_failure_is_retried_three_times_then_marked_failed() throws Exception {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(MimeMessage.class));
        String res = send(request(UUID.randomUUID().toString()), true).andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(res).get("notificationId").asText();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            String body = mvc.perform(get("/cs/v1/email/" + id).header("X-API-KEY", KEY))
                    .andExpect(jsonPath("$.status").value("FAILED")).andReturn().getResponse().getContentAsString();
            JsonNode n = json.readTree(body);
            assertThat(n.get("attempts").asInt()).isEqualTo(4); // first try + 3 retries
        });
    }

    @Test
    void unknown_notification_is_404() throws Exception {
        mvc.perform(get("/cs/v1/email/999999").header("X-API-KEY", KEY)).andExpect(status().isNotFound());
        mvc.perform(get("/cs/v1/email/abc").header("X-API-KEY", KEY)).andExpect(status().isNotFound());
    }

    @Test
    void health_is_open() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
}
