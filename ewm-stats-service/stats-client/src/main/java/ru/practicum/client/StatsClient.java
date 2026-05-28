package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.StatDto;

@Component
public class StatsClient {
    private final RestTemplate restTemplate;

    public StatsClient(
            @Value("${stats-server.url:http://localhost:9090}")
            String serverUrl
    ) {

        this.restTemplate = new RestTemplate();

        this.restTemplate.setUriTemplateHandler(
                new DefaultUriBuilderFactory(serverUrl)
        );
    }

    public ResponseEntity<Object> saveHit(StatDto dto) {
        HttpEntity<StatDto> requestEntity = new HttpEntity<>(dto);

        return restTemplate.exchange(
                "/hit",
                HttpMethod.POST,
                requestEntity,
                Object.class
        );
    }

    public ResponseEntity<Object> getStats(String params) {
        return restTemplate.exchange(
                "/stats?" + params,
                HttpMethod.GET,
                null,
                Object.class
        );
    }
}