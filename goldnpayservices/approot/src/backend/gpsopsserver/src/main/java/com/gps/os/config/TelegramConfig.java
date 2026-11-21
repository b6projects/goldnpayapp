package com.gps.os.config;

import com.gps.os.integration.TelegramApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramApiClient supportBotApi(RestClient.Builder externalRestClientBuilder, AppProperties props) {
        return new TelegramApiClient(externalRestClientBuilder, props.telegram().apiBase(),
                props.telegram().supportBotToken());
    }

    @Bean
    public TelegramApiClient prBotApi(RestClient.Builder externalRestClientBuilder, AppProperties props) {
        return new TelegramApiClient(externalRestClientBuilder, props.telegram().apiBase(),
                props.telegram().prBotToken());
    }
}
