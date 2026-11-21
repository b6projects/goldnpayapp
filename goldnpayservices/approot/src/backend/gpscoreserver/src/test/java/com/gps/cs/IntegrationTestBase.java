package com.gps.cs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gps.cs.dto.OpsDtos.EmailRequest;
import com.gps.cs.entity.*;
import com.gps.cs.integration.OpsApiService;
import com.gps.cs.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Boots the full app on H2 (MySQL mode) with the real Flyway script and a mocked Ops. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {
    protected static final String PASSWORD = "Passw0rdOK";

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UserRepository users;
    @Autowired protected PasswordEncoder encoder;
    @MockitoBean protected OpsApiService ops;

    @BeforeEach
    void opsAccepts() {
        when(ops.sendEmail(any())).thenReturn(true);
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /** Creates a user directly in the database, already ACTIVE. */
    protected User createUser(String email, Role role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(PASSWORD));
        u.setFullName("Test " + role);
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setKycStatus(KycStatus.NOT_SUBMITTED);
        return users.save(u);
    }

    protected JsonNode login(String email, String password) throws Exception {
        String body = json.writeValueAsString(java.util.Map.of("email", email, "password", password));
        String res = mvc.perform(post("/tc/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(res);
    }

    protected String tokenFor(String email, Role role) throws Exception {
        createUser(email, role);
        return login(email, PASSWORD).get("accessToken").asText();
    }

    /** Pulls the 6-digit code out of the most recent OTP email handed to the mocked Ops. */
    protected String lastOtp() {
        ArgumentCaptor<EmailRequest> cap = ArgumentCaptor.forClass(EmailRequest.class);
        verify(ops, atLeastOnce()).sendEmail(cap.capture());
        List<EmailRequest> all = cap.getAllValues();
        Matcher m = Pattern.compile("<strong>(\\d{6})</strong>").matcher(all.get(all.size() - 1).content());
        if (!m.find()) throw new AssertionError("no OTP in last email");
        return m.group(1);
    }

    protected static String bearer(String token) {
        return "Bearer " + token;
    }
}
