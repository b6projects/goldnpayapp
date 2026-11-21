package com.gps.cs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.gps.cs.entity.BankAccount;
import com.gps.cs.entity.Role;
import com.gps.cs.entity.User;
import com.gps.cs.repository.BankAccountRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class AdminAndClientFlowTest extends IntegrationTestBase {
    @Autowired BankAccountRepository banks;

    private String body(Object o) throws Exception {
        return json.writeValueAsString(o);
    }

    private static final byte[] PDF = "%PDF-1.7\nhello".getBytes();

    @Test
    void manager_cannot_create_admin_but_admin_can_create_manager() throws Exception {
        String manager = tokenFor(uniqueEmail("mgr"), Role.MANAGER);
        String admin = tokenFor(uniqueEmail("adm"), Role.ADMIN);

        mvc.perform(post("/tc/v1/manager/staff").header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", uniqueEmail("x"), "fullName", "Evil", "role", "ADMIN"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/tc/v1/manager/staff").header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", uniqueEmail("x"), "fullName", "Peer", "role", "MANAGER"))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/tc/v1/manager/staff").header("Authorization", bearer(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", uniqueEmail("s"), "fullName", "Sam Sales", "role", "SALES"))))
                .andExpect(status().isCreated());

        String newMgr = uniqueEmail("newmgr");
        String res = mvc.perform(post("/tc/v1/manager/staff").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("email", newMgr, "fullName", "New Manager", "role", "MANAGER"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Long id = json.readTree(res).get("id").asLong();
        verify(ops, atLeastOnce()).sendEmailAsync(any());

        // suspend revokes tokens; reinstate restores login ability
        String mgrToken = tokenForExisting(newMgr);
        mvc.perform(put("/tc/v1/manager/staff/" + id + "/suspend").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUSPENDED"));
        mvc.perform(get("/tc/v1/admin/users").header("Authorization", bearer(mgrToken)))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/tc/v1/manager/staff/" + id + "/reinstate").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
        mvc.perform(delete("/tc/v1/manager/staff/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        assertThat(users.findById(id).orElseThrow().getStatus().name()).isEqualTo("DELETED");
    }

    /** Staff are created with an unusable random password; give the user a known one for the test. */
    private String tokenForExisting(String email) throws Exception {
        User u = users.findByEmailIgnoreCase(email).orElseThrow();
        u.setPasswordHash(encoder.encode(PASSWORD));
        users.save(u);
        return login(email, PASSWORD).get("accessToken").asText();
    }

    @Test
    void gold_rate_add_pause_resume() throws Exception {
        String admin = tokenFor(uniqueEmail("rate"), Role.ADMIN);
        mvc.perform(post("/tc/v1/admin/gold-rate").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("rate24k", 7500.50, "rate22k", 6875.00, "rate18k", 5625.00))))
                .andExpect(status().isCreated());
        mvc.perform(get("/tc/v1/public/gold-rate/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVE"))
                .andExpect(jsonPath("$.rate24k").value(7500.50));

        mvc.perform(put("/tc/v1/admin/gold-rate/pause").header("Authorization", bearer(admin))).andExpect(status().isOk());
        mvc.perform(get("/tc/v1/public/gold-rate/live"))
                .andExpect(jsonPath("$.status").value("PAUSED"))
                .andExpect(jsonPath("$.rate24k").doesNotExist());

        mvc.perform(put("/tc/v1/admin/gold-rate/resume").header("Authorization", bearer(admin))).andExpect(status().isOk());
        mvc.perform(get("/tc/v1/public/gold-rate/live")).andExpect(jsonPath("$.status").value("LIVE"));

        mvc.perform(post("/tc/v1/admin/gold-rate").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("rate24k", 100, "rate22k", 200, "rate18k", 50))))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/tc/v1/admin/gold-rate/history").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void new_rate_is_announced_through_ops_only_when_enabled() throws Exception {
        String admin = tokenFor(uniqueEmail("ann"), Role.ADMIN);
        mvc.perform(put("/tc/v1/admin/settings/social-media").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("settings", Map.of("post_on_rate_change", "true", "twitter.enabled", "true")))))
                .andExpect(status().isOk());
        mvc.perform(post("/tc/v1/admin/gold-rate").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("rate24k", 7600, "rate22k", 6900, "rate18k", 5700))))
                .andExpect(status().isCreated());
        verify(ops).publishSocialPostAsync(argThat(r -> r.channels().equals(java.util.List.of("TWITTER")) && r.overlayRate()));
    }

    @Test
    void settings_public_vs_private() throws Exception {
        String admin = tokenFor(uniqueEmail("set"), Role.ADMIN);
        mvc.perform(put("/tc/v1/admin/settings/app").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("settings", Map.of("support.phone", "+911234567890")))))
                .andExpect(status().isOk());
        mvc.perform(put("/tc/v1/admin/settings/system").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("settings", Map.of("internal.flag", "x")))))
                .andExpect(status().isOk());
        mvc.perform(get("/tc/v1/public/app-settings"))
                .andExpect(jsonPath("$['support.phone']").value("+911234567890"))
                .andExpect(jsonPath("$['internal.flag']").doesNotExist());
        String marketing = tokenFor(uniqueEmail("mkt"), Role.MARKETING);
        mvc.perform(get("/tc/v1/admin/settings/system").header("Authorization", bearer(marketing)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/tc/v1/admin/settings/social-media").header("Authorization", bearer(marketing)))
                .andExpect(status().isOk());
    }

    @Test
    void kyc_upload_review_and_access_rules() throws Exception {
        String email = uniqueEmail("kyc");
        String client = tokenFor(email, Role.CLIENT);
        Long clientId = users.findByEmailIgnoreCase(email).orElseThrow().getId();

        mvc.perform(multipart("/tc/v1/client/kyc").file(new MockMultipartFile("files", "pan.pdf", "application/pdf", PDF))
                        .param("docTypes", "PAN").header("Authorization", bearer(client)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.documents[0].filePath").doesNotExist());

        // wrong content despite a .pdf name
        mvc.perform(multipart("/tc/v1/client/kyc")
                        .file(new MockMultipartFile("files", "evil.pdf", "application/pdf", "MZ-not-a-pdf".getBytes()))
                        .param("docTypes", "OTHER").header("Authorization", bearer(client)))
                .andExpect(status().isUnsupportedMediaType());
        // not an allowed type at all
        mvc.perform(multipart("/tc/v1/client/kyc")
                        .file(new MockMultipartFile("files", "a.txt", "text/plain", "hello world".getBytes()))
                        .param("docTypes", "OTHER").header("Authorization", bearer(client)))
                .andExpect(status().isUnsupportedMediaType());
        // over 5 MB
        mvc.perform(multipart("/tc/v1/client/kyc")
                        .file(new MockMultipartFile("files", "big.pdf", "application/pdf", bigPdf()))
                        .param("docTypes", "OTHER").header("Authorization", bearer(client)))
                .andExpect(status().is(413));

        String docs = mvc.perform(get("/tc/v1/client/kyc").header("Authorization", bearer(client)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long docId = json.readTree(docs).get("documents").get(0).get("id").asLong();

        // a client cannot download, an admin can
        mvc.perform(get("/tc/v1/admin/kyc/" + clientId + "/documents/" + docId).header("Authorization", bearer(client)))
                .andExpect(status().isForbidden());
        String admin = tokenFor(uniqueEmail("kycadm"), Role.ADMIN);
        mvc.perform(get("/tc/v1/admin/kyc/" + clientId + "/documents/" + docId).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(content().bytes(PDF));
        mvc.perform(get("/tc/v1/admin/kyc/pending").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());

        // reject needs a reason; approve emails the client through Ops
        mvc.perform(put("/tc/v1/admin/kyc/" + clientId + "/review").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("approve", false))))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/tc/v1/admin/kyc/" + clientId + "/review").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("approve", true))))
                .andExpect(status().isNoContent());
        mvc.perform(get("/tc/v1/client/kyc").header("Authorization", bearer(client)))
                .andExpect(jsonPath("$.status").value("APPROVED"));
        verify(ops, atLeastOnce()).sendEmailAsync(argThat(e -> "KYC_RESULT".equals(e.category())));
    }

    private static byte[] bigPdf() {
        byte[] b = new byte[5 * 1024 * 1024 + 10];
        System.arraycopy("%PDF-1.7".getBytes(), 0, b, 0, 8);
        return b;
    }

    @Test
    void bank_account_numbers_are_encrypted_at_rest_and_masked_in_responses() throws Exception {
        String client = tokenFor(uniqueEmail("bank"), Role.CLIENT);
        String res = mvc.perform(post("/tc/v1/client/bank-accounts").header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("accountHolder", "Priya S", "accountNumber", "123456789012",
                                "ifsc", "HDFC0001234", "bankName", "HDFC Bank"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumberMasked").value("XXXXXX9012"))
                .andExpect(jsonPath("$.primary").value(true))
                .andReturn().getResponse().getContentAsString();
        assertThat(res).doesNotContain("123456789012");

        long id = json.readTree(res).get("id").asLong();
        BankAccount stored = banks.findById(id).orElseThrow();
        assertThat(stored.getAccountNumber()).doesNotContain("123456789012").isNotBlank();
        assertThat(stored.getAccountLast4()).isEqualTo("9012");

        // second account is not primary; switching works
        String second = mvc.perform(post("/tc/v1/client/bank-accounts").header("Authorization", bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("accountHolder", "Priya S", "accountNumber", "99887766554433",
                                "ifsc", "SBIN0000001", "bankName", "SBI"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.primary").value(false))
                .andReturn().getResponse().getContentAsString();
        long id2 = json.readTree(second).get("id").asLong();
        mvc.perform(put("/tc/v1/client/bank-accounts/" + id2 + "/primary").header("Authorization", bearer(client)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.primary").value(true));
        mvc.perform(get("/tc/v1/client/bank-accounts").header("Authorization", bearer(client)))
                .andExpect(jsonPath("$[0].primary").value(false)).andExpect(jsonPath("$[1].primary").value(true));

        // another client cannot touch it
        String other = tokenFor(uniqueEmail("bank2"), Role.CLIENT);
        mvc.perform(delete("/tc/v1/client/bank-accounts/" + id).header("Authorization", bearer(other)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/tc/v1/client/bank-accounts/" + id2).header("Authorization", bearer(client)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/tc/v1/client/bank-accounts").header("Authorization", bearer(client)))
                .andExpect(jsonPath("$[0].primary").value(true)); // promoted
    }

    @Test
    void admin_user_listing_filters_and_activation() throws Exception {
        String admin = tokenFor(uniqueEmail("list"), Role.ADMIN);
        String email = uniqueEmail("pending");
        mvc.perform(post("/tc/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", PASSWORD, "fullName", "Pending Person")))).andExpect(status().isCreated());
        Long id = users.findByEmailIgnoreCase(email).orElseThrow().getId();

        mvc.perform(get("/tc/v1/admin/users").param("status", "PENDING_ACTIVATION").param("q", "pending person")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].email").value(email));
        mvc.perform(get("/tc/v1/admin/users/pending-activation").header("Authorization", bearer(admin)))
                .andExpect(status().isOk());
        mvc.perform(put("/tc/v1/admin/users/" + id + "/activate").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACTIVE"));
        mvc.perform(get("/tc/v1/admin/summary/roles").header("Authorization", bearer(admin))).andExpect(status().isOk());
        mvc.perform(get("/tc/v1/admin/summary/clients").header("Authorization", bearer(admin))).andExpect(status().isOk());
        mvc.perform(get("/tc/v1/admin/users").param("sort", "nope").header("Authorization", bearer(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/tc/v1/admin/reports/payments").header("Authorization", bearer(admin)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void telegram_link_code_is_single_use_and_verified_by_ops() throws Exception {
        String client = tokenFor(uniqueEmail("tg"), Role.CLIENT);
        String res = mvc.perform(post("/tc/v1/client/telegram/link-code").header("Authorization", bearer(client)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").isString()).andReturn().getResponse().getContentAsString();
        String code = json.readTree(res).get("code").asText();
        assertThat(code).hasSize(8);

        mvc.perform(post("/tc/v1/internal/telegram/link-code/verify").header("X-API-KEY", "test-ops-to-core-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("code", code))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true)).andExpect(jsonPath("$.userId").exists());
        mvc.perform(post("/tc/v1/internal/telegram/link-code/verify").header("X-API-KEY", "test-ops-to-core-key")
                        .contentType(MediaType.APPLICATION_JSON).content(body(Map.of("code", code))))
                .andExpect(jsonPath("$.valid").value(false));
        mvc.perform(get("/tc/v1/client/telegram").header("Authorization", bearer(client)))
                .andExpect(jsonPath("$.linked").value(false));
    }

    @Test
    void social_post_goes_through_ops() throws Exception {
        String marketing = tokenFor(uniqueEmail("soc"), Role.MARKETING);
        when(ops.publishSocialPost(any())).thenReturn(java.util.Optional.of(json.readTree("{\"TWITTER\":\"SENT\"}")));
        mvc.perform(post("/tc/v1/admin/social/post").header("Authorization", bearer(marketing))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("text", "Diwali offer", "channels", java.util.List.of("TWITTER")))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.TWITTER").value("SENT"));
        mvc.perform(post("/tc/v1/admin/social/post").header("Authorization", bearer(marketing))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("text", "x", "channels", java.util.List.of("FACEBOOK")))))
                .andExpect(status().isBadRequest());
        when(ops.publishSocialPost(any())).thenReturn(java.util.Optional.empty());
        mvc.perform(post("/tc/v1/admin/social/post").header("Authorization", bearer(marketing))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(Map.of("text", "x", "channels", java.util.List.of("DISCORD")))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void homepage_images_upload_list_serve_delete() throws Exception {
        String marketing = tokenFor(uniqueEmail("img"), Role.MARKETING);
        byte[] png = new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2, 3};
        String res = mvc.perform(multipart("/tc/v1/admin/homepage-images")
                        .file(new MockMultipartFile("file", "b.png", "image/png", png)).param("title", "Banner")
                        .header("Authorization", bearer(marketing)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = json.readTree(res).get("id").asLong();
        mvc.perform(get("/tc/v1/public/homepage-images")).andExpect(jsonPath("$[0].title").value("Banner"));
        mvc.perform(get("/tc/v1/public/images/" + id)).andExpect(status().isOk()).andExpect(content().bytes(png));
        // pdf is not an image
        mvc.perform(multipart("/tc/v1/admin/event-images")
                        .file(new MockMultipartFile("file", "x.pdf", "application/pdf", PDF))
                        .header("Authorization", bearer(marketing)))
                .andExpect(status().isUnsupportedMediaType());
        mvc.perform(delete("/tc/v1/admin/homepage-images/" + id).header("Authorization", bearer(marketing)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/tc/v1/public/images/" + id)).andExpect(status().isNotFound());
    }
}
