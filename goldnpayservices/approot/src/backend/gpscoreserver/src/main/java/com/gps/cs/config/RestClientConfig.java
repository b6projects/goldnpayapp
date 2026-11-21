package com.gps.cs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final AppProperties props;

    /** Core -> Ops client: connect 3s, read 10s, authenticated with the Core->Ops key. */
    @Bean
    public RestClient opsRestClient() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(props.ops().connectTimeoutMs());
        f.setReadTimeout(props.ops().readTimeoutMs());
        return RestClient.builder()
                .baseUrl(props.ops().baseUrl())
                .requestFactory(f)
                .defaultHeader("X-API-KEY", props.security().coreToOpsApiKey())
                .build();
    }
}
