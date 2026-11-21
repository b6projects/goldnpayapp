package com.gps.os.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final AppProperties props;

    /** Ops -> Core client, authenticated with the Ops->Core key. */
    @Bean
    public RestClient coreRestClient() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(props.core().connectTimeoutMs());
        f.setReadTimeout(props.core().readTimeoutMs());
        return RestClient.builder()
                .baseUrl(props.core().baseUrl())
                .requestFactory(f)
                .defaultHeader("X-API-KEY", props.security().opsToCoreApiKey())
                .build();
    }

    /** Plain client for third-party APIs (Telegram, Discord). No credentials are attached here. */
    @Bean
    public RestClient.Builder externalRestClientBuilder() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000);
        f.setReadTimeout(40_000); // Telegram long polling holds the request open
        return RestClient.builder().requestFactory(f);
    }
}
