package ru.practicum.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate restTemplate;

    public StatsClient(@Value("${stats-server.url:http://localhost:9090}") String serverUrl) {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(serverUrl));
    }

    public ResponseEntity<Object> saveHit(StatDto dto) {
        HttpEntity<StatDto> requestEntity = new HttpEntity<>(dto);
        try {
            return restTemplate.exchange("/hit", HttpMethod.POST, requestEntity, Object.class);
        } catch (RestClientException exception) {
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

    public ResponseEntity<List<ViewStatDto>> getStats(LocalDateTime start, LocalDateTime end,
                                                      List<String> uris, boolean unique) {
        String uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", FORMATTER.format(start))
                .queryParam("end", FORMATTER.format(end))
                .queryParam("unique", unique)
                .queryParam("uris", uris)
                .build()
                .toUriString();

        try {
            return restTemplate.exchange(uri, HttpMethod.GET, null, new ParameterizedTypeReference<>() {
            });
        } catch (RestClientException exception) {
            return ResponseEntity.ok(List.of());
        }
    }
}
