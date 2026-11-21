package com.gps.cs;

import static org.assertj.core.api.Assertions.assertThat;

import com.gps.cs.config.AppProperties;
import com.gps.cs.config.RestClientConfig;
import com.gps.cs.dto.OpsDtos.EmailRequest;
import com.gps.cs.dto.OpsDtos.SocialPostRequest;
import com.gps.cs.integration.OpsApiService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Core -> Ops client against a stub Ops: contract shape, API key, retries and failure handling. */
class OpsApiServiceTest {
    private HttpServer server;
    private final List<String> bodies = Collections.synchronizedList(new ArrayList<>());
    private final List<String> keys = Collections.synchronizedList(new ArrayList<>());
    private final List<String> paths = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger calls = new AtomicInteger();

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    /** Starts a stub that answers with the given statuses in order (the last one repeats). */
    private OpsApiService opsAnswering(int... statuses) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            int n = calls.getAndIncrement();
            paths.add(ex.getRequestURI().getPath());
            keys.add(ex.getRequestHeaders().getFirst("X-API-KEY"));
            bodies.add(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            int status = statuses[Math.min(n, statuses.length - 1)];
            byte[] resp = "{\"notificationId\":\"1\",\"status\":\"QUEUED\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, resp.length);
            ex.getResponseBody().write(resp);
            ex.close();
        });
        server.start();
        return service("http://127.0.0.1:" + server.getAddress().getPort());
    }

    private static OpsApiService service(String baseUrl) {
        var props = new AppProperties("TEST", null, new AppProperties.Security("core-to-ops", "ops-to-core", "k", 20),
                null, new AppProperties.Ops(baseUrl, 500, 2000, 2), null, null, null, null);
        return new OpsApiService(new RestClientConfig(props).opsRestClient(), props);
    }

    private static EmailRequest email() {
        return new EmailRequest("req-1", "STANDARD", List.of("a@example.com"), List.of(), List.of(), "Hi",
                "<p>Hello</p>", List.of(), "WELCOME", 7L);
    }

    @Test
    void sends_the_contract_json_to_the_ops_email_endpoint_with_the_core_to_ops_key() throws Exception {
        OpsApiService ops = opsAnswering(202);
        assertThat(ops.sendEmail(email())).isTrue();
        assertThat(paths).containsExactly("/cs/v1/email/send");
        assertThat(keys).containsExactly("core-to-ops");
        assertThat(bodies.get(0)).contains("\"requestId\":\"req-1\"").contains("\"templateCode\":\"STANDARD\"")
                .contains("\"to\":[\"a@example.com\"]").contains("\"content\":\"<p>Hello</p>\"")
                .contains("\"category\":\"WELCOME\"").contains("\"userId\":7");
    }

    @Test
    void retries_server_errors_twice_then_succeeds() throws Exception {
        OpsApiService ops = opsAnswering(503, 503, 202);
        assertThat(ops.sendEmail(email())).isTrue();
        assertThat(calls.get()).isEqualTo(3);
        assertThat(bodies).allMatch(b -> b.contains("\"requestId\":\"req-1\"")); // same idempotency key on every try
    }

    @Test
    void gives_up_after_two_retries() throws Exception {
        OpsApiService ops = opsAnswering(500);
        assertThat(ops.sendEmail(email())).isFalse();
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void client_errors_are_not_retried() throws Exception {
        OpsApiService ops = opsAnswering(400);
        assertThat(ops.sendEmail(email())).isFalse();
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void an_unreachable_ops_returns_false_instead_of_throwing() {
        assertThat(service("http://127.0.0.1:9").sendEmail(email())).isFalse();
    }

    @Test
    void social_and_unlink_calls_use_the_documented_paths() throws Exception {
        OpsApiService ops = opsAnswering(200);
        assertThat(ops.publishSocialPost(new SocialPostRequest("r", "t", null, List.of("TWITTER"), true))).isPresent();
        assertThat(ops.unlinkTelegram(42L)).isTrue();
        assertThat(paths).containsExactly("/cs/v1/social/post", "/cs/v1/bauth/tgmdisconnect");
        assertThat(bodies.get(0)).contains("\"channels\":[\"TWITTER\"]").contains("\"overlayRate\":true");
        assertThat(bodies.get(1)).contains("\"userId\":42");
    }
}
