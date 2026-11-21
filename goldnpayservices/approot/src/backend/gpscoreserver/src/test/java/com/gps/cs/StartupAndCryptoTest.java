package com.gps.cs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gps.cs.config.AppProperties;
import com.gps.cs.util.FieldEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

class StartupAndCryptoTest {

    @Test
    void server_refuses_to_start_when_required_secrets_are_missing() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(GpsCoreServerApplication.class)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .properties("spring.profiles.active=nosuchprofile")
                .run())
                .hasStackTraceContaining("Missing required configuration")
                .hasStackTraceContaining("JWT_SECRET_KEY");
    }

    private static FieldEncryptor encryptor(String key) {
        var sec = new AppProperties.Security("a", "b", key, 20);
        return new FieldEncryptor(new AppProperties("TEST", null, sec, null, null, null, null, null, null));
    }

    @Test
    void field_encryption_round_trips_and_is_randomised() {
        FieldEncryptor e = encryptor("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        String c1 = e.encrypt("123456789012");
        String c2 = e.encrypt("123456789012");
        assertThat(c1).isNotEqualTo(c2).doesNotContain("123456789012");
        assertThat(e.decrypt(c1)).isEqualTo("123456789012");
    }

    @Test
    void tampered_ciphertext_is_rejected() {
        FieldEncryptor e = encryptor("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        byte[] raw = java.util.Base64.getDecoder().decode(e.encrypt("secret"));
        raw[raw.length - 1] ^= 0x01; // flip one bit of the GCM tag
        String tampered = java.util.Base64.getEncoder().encodeToString(raw);
        assertThatThrownBy(() -> e.decrypt(tampered)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void short_encryption_key_is_rejected() {
        assertThatThrownBy(() -> encryptor("c2hvcnQ=")).isInstanceOf(IllegalStateException.class);
    }
}
