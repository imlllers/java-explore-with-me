package ru.practicum.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body);

        return rest.exchange(
                path,
                HttpMethod.POST,
                requestEntity,
                Object.class
        );
    }

    protected ResponseEntity<Object> get(String path) {
        return rest.exchange(
                path,
                HttpMethod.GET,
                null,
                Object.class
        );
    }
}