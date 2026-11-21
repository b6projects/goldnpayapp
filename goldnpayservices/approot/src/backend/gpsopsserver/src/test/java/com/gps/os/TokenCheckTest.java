package com.gps.os;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class TokenCheckTest extends IntegrationTestBase {
    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private String jwt(String secret, String typ, long ttlMs) {
        return Jwts.builder().setSubject("a@example.com").claim("uid", 7L).claim("role", "CLIENT").claim("typ", typ)
                .setExpiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)), SignatureAlgorithm.HS256).compact();
    }

    private ResultActions check(String token) throws Exception {
        return mvc.perform(post("/cs/v1/tokencheck").header("X-API-KEY", KEY).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("token", token))));
    }

    @Test
    void a_core_issued_access_token_validates_locally() throws Exception {
        check(jwt(SECRET, "ACCESS", 60_000)).andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.userId").value(7)).andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void expired_forged_refresh_and_garbage_tokens_are_invalid() throws Exception {
        check(jwt(SECRET, "ACCESS", -1000)).andExpect(jsonPath("$.valid").value(false));
        check(jwt("QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVowMTIzNDU2Nzg5MTI=", "ACCESS", 60_000)).andExpect(jsonPath("$.valid").value(false));
        check(jwt(SECRET, "REFRESH", 60_000)).andExpect(jsonPath("$.valid").value(false));
        check("not.a.jwt").andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void needs_the_api_key() throws Exception {
        mvc.perform(post("/cs/v1/tokencheck").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }
}
