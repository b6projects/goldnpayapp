package com.gps.cs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.gps.cs.entity.User;
import com.gps.cs.entity.UserStatus;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthFlowTest extends IntegrationTestBase {

    private String body(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    @Test
    void register_otp_verify_login_refresh_logout_and_revoked_token_is_rejected() throws Exception {
        String email = uniqueEmail("flow");
        mvc.perform(post("/tc/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", PASSWORD, "fullName", "Priya S"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_ACTIVATION"));
        verify(ops).sendEmailAsync(any()); // welcome email is fire-and-forget

        // not activated yet
        mvc.perform(post("/tc/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_ACTIVATED"));

        mvc.perform(post("/tc/v1/auth/otp/request").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "purpose", "ACTIVATION"))))
                .andExpect(status().isAccepted());
        String code = lastOtp();

        mvc.perform(post("/tc/v1/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "purpose", "ACTIVATION", "code", code))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.verified").value(true));
        // single use
        mvc.perform(post("/tc/v1/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "purpose", "ACTIVATION", "code", code))))
                .andExpect(status().isBadRequest());

        JsonNode tokens = login(email, PASSWORD);
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();

        mvc.perform(get("/tc/v1/client/profile").header("Authorization", bearer(access)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));

        String refreshed = mvc.perform(post("/tc/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String newAccess = json.readTree(refreshed).get("accessToken").asText();
        assertThat(newAccess).isNotEqualTo(access);

        mvc.perform(post("/tc/v1/auth/logout").header("Authorization", bearer(newAccess)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/tc/v1/client/profile").header("Authorization", bearer(access)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/tc/v1/client/profile").header("Authorization", bearer(newAccess)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/tc/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void otp_email_failure_returns_503() throws Exception {
        String email = uniqueEmail("otpfail");
        mvc.perform(post("/tc/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", PASSWORD, "fullName", "A B")))).andExpect(status().isCreated());
        when(ops.sendEmail(any())).thenReturn(false);
        mvc.perform(post("/tc/v1/auth/otp/request").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "purpose", "ACTIVATION"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("OTP_DELIVERY_FAILED"));
    }

    @Test
    void unknown_email_otp_request_does_not_reveal_anything() throws Exception {
        mvc.perform(post("/tc/v1/auth/otp/request").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", "nobody@example.com", "purpose", "PASSWORD_RESET"))))
                .andExpect(status().isAccepted());
        verify(ops, never()).sendEmail(any());
    }

    @Test
    void password_reset_flow() throws Exception {
        String email = uniqueEmail("reset");
        createUser(email, com.gps.cs.entity.Role.CLIENT);
        mvc.perform(post("/tc/v1/auth/otp/request").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "purpose", "PASSWORD_RESET")))).andExpect(status().isAccepted());
        String code = lastOtp();
        String res = mvc.perform(post("/tc/v1/auth/otp/verify").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "purpose", "PASSWORD_RESET", "code", code))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String grant = json.readTree(res).get("resetToken").asText();

        mvc.perform(post("/tc/v1/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "resetToken", grant, "newPassword", "NewPassw0rd"))))
                .andExpect(status().isOk());
        login(email, "NewPassw0rd");
        // grant is single use
        mvc.perform(post("/tc/v1/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "resetToken", grant, "newPassword", "Another1Pass"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void five_wrong_passwords_lock_the_account() throws Exception {
        String email = uniqueEmail("lock");
        createUser(email, com.gps.cs.entity.Role.CLIENT);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/tc/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(body(Map.of("email", email, "password", "wrong-pass-1"))))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/tc/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isLocked());
        User u = users.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(u.getLockedUntil()).isNotNull();
    }

    @Test
    void validation_errors_use_problem_detail() throws Exception {
        mvc.perform(post("/tc/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", "not-an-email", "password", "short", "fullName", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/tc/v1/auth/register"))
                .andExpect(jsonPath("$.traceId").exists());
    }

    @Test
    void protected_endpoints_need_a_token_and_the_right_role() throws Exception {
        mvc.perform(get("/tc/v1/client/profile")).andExpect(status().isUnauthorized());
        String clientToken = tokenFor(uniqueEmail("c"), com.gps.cs.entity.Role.CLIENT);
        mvc.perform(get("/tc/v1/admin/users").header("Authorization", bearer(clientToken)))
                .andExpect(status().isForbidden());
        String adminToken = tokenFor(uniqueEmail("a"), com.gps.cs.entity.Role.ADMIN);
        mvc.perform(get("/tc/v1/client/profile").header("Authorization", bearer(adminToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void internal_endpoints_need_the_ops_api_key() throws Exception {
        String payload = body(Map.of("token", "whatever"));
        mvc.perform(post("/tc/v1/internal/tokencheck").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/tc/v1/internal/tokencheck").header("X-API-KEY", "wrong")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());
        // the Core->Ops key must not open the Ops->Core door
        mvc.perform(post("/tc/v1/internal/tokencheck").header("X-API-KEY", "test-core-to-ops-key")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/tc/v1/internal/tokencheck").header("X-API-KEY", "test-ops-to-core-key")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false));

        String access = tokenFor(uniqueEmail("tc"), com.gps.cs.entity.Role.CLIENT);
        mvc.perform(post("/tc/v1/internal/tokencheck").header("X-API-KEY", "test-ops-to-core-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("token", access))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void health_and_public_endpoints_are_open() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/tc/v1/public/gold-rate/live")).andExpect(status().isOk());
        mvc.perform(get("/tc/v1/public/reports")).andExpect(status().isOk());
    }

    @Test
    void swagger_is_reachable_in_non_prd_profiles() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
