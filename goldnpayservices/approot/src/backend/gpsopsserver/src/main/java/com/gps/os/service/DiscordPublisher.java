package com.gps.os.service;

import com.gps.os.config.AppProperties;
import java.net.URI;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Posts to a Discord channel through its webhook. The webhook URL is a secret and is never logged. */
@Service
public class DiscordPublisher implements ChannelPublisher {
    private static final int LIMIT = 2000;
    private final RestClient client;
    private final URI webhook;

    public DiscordPublisher(RestClient.Builder externalRestClientBuilder, AppProperties props) {
        this.client = externalRestClientBuilder.clone().build();
        this.webhook = URI.create(props.discord().webhookUrl());
    }

    @Override
    public String channel() {
        return "DISCORD";
    }

    @Override
    public void publish(String text, byte[] image) {
        String content = text.length() > LIMIT ? text.substring(0, LIMIT - 1) + "…" : text;
        try {
            if (image == null || image.length == 0) {
                client.post().uri(webhook).contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("content", content)).retrieve().toBodilessEntity();
            } else {
                MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
                HttpHeaders json = new HttpHeaders();
                json.setContentType(MediaType.APPLICATION_JSON);
                form.add("payload_json", new HttpEntity<>(Map.of("content", content), json));
                HttpHeaders file = new HttpHeaders();
                file.setContentType(MediaType.IMAGE_PNG);
                form.add("files[0]", new HttpEntity<>(new ByteArrayResource(image) {
                    @Override
                    public String getFilename() {
                        return "gold.png";
                    }
                }, file));
                client.post().uri(webhook).contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(form).retrieve().toBodilessEntity();
            }
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Discord rejected the post: HTTP " + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new IllegalStateException("Discord unreachable");
        }
    }
}
