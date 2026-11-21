package com.gps.os;

import static org.assertj.core.api.Assertions.assertThat;

import com.gps.os.config.AppProperties;
import com.gps.os.config.RestClientConfig;
import com.gps.os.integration.BotApiService;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Ops -> Core client against a stub Core: contract shape, API key and failure handling. */
class BotApiServiceTest {
    private HttpServer server;
    private final List<String> keys = Collections.synchronizedList(new ArrayList<>());
    private final List<String> bodies = Collections.synchronizedList(new ArrayList<>());

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    private BotApiService coreAnswering(int status, String json) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            keys.add(ex.getRequestHeaders().getFirst("X-API-KEY"));
            bodies.add(ex.getRequestURI().getPath() + " " + new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] b = json.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        server.start();
        return service("http://127.0.0.1:" + server.getAddress().getPort());
    }

    private static BotApiService service(String baseUrl) {
        var props = new AppProperties("TEST", null, new AppProperties.Security("core-to-ops", "ops-to-core"),
                new AppProperties.Core(baseUrl, 500, 2000), null, null, null, null, null, null);
        return new BotApiService(new RestClientConfig(props).coreRestClient());
    }

    @Test
    void a_valid_link_code_returns_the_user_and_sends_the_ops_to_core_key() throws Exception {
        BotApiService core = coreAnswering(200, "{\"valid\":true,\"userId\":55}");
        var r = core.verifyLinkCode("ABCD2345");
        assertThat(r.valid()).isTrue();
        assertThat(r.userId()).isEqualTo(55L);
        assertThat(keys).containsExactly("ops-to-core");
        assertThat(bodies.get(0)).isEqualTo("/tc/v1/internal/telegram/link-code/verify {\"code\":\"ABCD2345\"}");
    }

    @Test
    void an_invalid_code_is_reachable_but_not_valid() throws Exception {
        var r = coreAnswering(200, "{\"valid\":false,\"userId\":null}").verifyLinkCode("NOPE");
        assertThat(r.reachable()).isTrue();
        assertThat(r.valid()).isFalse();
    }

    @Test
    void core_errors_and_outages_are_reported_as_unreachable() throws Exception {
        assertThat(coreAnswering(401, "{}").verifyLinkCode("X").reachable()).isFalse();
        server.stop(0);
        assertThat(service("http://127.0.0.1:9").verifyLinkCode("X").reachable()).isFalse();
    }

    @Test
    void token_check_maps_the_core_response() throws Exception {
        var res = coreAnswering(200, "{\"valid\":true,\"userId\":9,\"email\":\"a@example.com\",\"role\":\"CLIENT\"}").tokenCheck("jwt");
        assertThat(res).isPresent();
        assertThat(res.get().role()).isEqualTo("CLIENT");
        assertThat(bodies.get(0)).startsWith("/tc/v1/internal/tokencheck ");
    }
}
