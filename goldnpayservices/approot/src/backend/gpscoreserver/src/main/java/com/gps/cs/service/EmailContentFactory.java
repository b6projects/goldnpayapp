package com.gps.cs.service;

import com.gps.cs.dto.OpsDtos.EmailRequest;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/** Builds the body content only. Layout, branding, header and footer belong to Ops (D1). */
@Component
public class EmailContentFactory {

    public EmailRequest otp(User user, String purposeLabel, String code, int ttlMinutes) {
        String body = "<p>Hi " + esc(user.getFullName()) + ",</p>"
                + "<p>Your GoldNPay one-time code for " + esc(purposeLabel) + " is:</p>"
                + "<p style=\"font-size:24px;letter-spacing:4px\"><strong>" + code + "</strong></p>"
                + "<p>It is valid for " + ttlMinutes + " minutes and can be used once. "
                + "If you did not ask for it, you can ignore this email.</p>";
        return build(user, "TRANSACTIONAL", "Your GoldNPay verification code", body, "OTP");
    }

    public EmailRequest welcome(User user) {
        String body = "<p>Hi " + esc(user.getFullName()) + ",</p>"
                + "<p>Welcome to GoldNPay. Your account has been created. "
                + "Please verify your email with the code we send you to activate it.</p>";
        return build(user, "STANDARD", "Welcome to GoldNPay", body, "WELCOME");
    }

    public EmailRequest staffWelcome(User user, Role role) {
        String body = "<p>Hi " + esc(user.getFullName()) + ",</p>"
                + "<p>A GoldNPay " + role.name().toLowerCase() + " account has been created for you.</p>"
                + "<p>To set your password, use \"Forgot password\" on the sign-in page with this email address.</p>";
        return build(user, "STANDARD", "Your GoldNPay staff account", body, "STAFF_WELCOME");
    }

    public EmailRequest kycResult(User user, boolean approved, String reason) {
        String body = "<p>Hi " + esc(user.getFullName()) + ",</p>"
                + (approved
                ? "<p>Your KYC has been approved. You can now use all GoldNPay services.</p>"
                : "<p>Your KYC could not be approved.</p><p>Reason: " + esc(reason) + "</p>"
                + "<p>Please upload corrected documents.</p>");
        return build(user, "TRANSACTIONAL", approved ? "KYC approved" : "KYC needs attention", body, "KYC_RESULT");
    }

    public EmailRequest passwordChanged(User user) {
        String body = "<p>Hi " + esc(user.getFullName()) + ",</p>"
                + "<p>Your GoldNPay password was changed. If this was not you, contact support immediately.</p>";
        return build(user, "TRANSACTIONAL", "Your password was changed", body, "PASSWORD_RESET");
    }

    private EmailRequest build(User user, String template, String subject, String body, String category) {
        return new EmailRequest(UUID.randomUUID().toString(), template, List.of(user.getEmail()), List.of(), List.of(),
                subject, body, List.of(), category, user.getId());
    }

    private static String esc(String s) {
        return s == null ? "" : HtmlUtils.htmlEscape(s);
    }
}
