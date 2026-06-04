package ru.yandex.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.ViewStatDto;
import ru.yandex.practicum.client.StatsClient;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventStatService {
    private static final LocalDateTime STATS_START = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public void fillStats(Event event) {
        List<Event> events = new ArrayList<>();
        events.add(event);
        fillStats(events);
    }

    public void fillStats(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (Event event : events) {
            ids.add(event.getId());
        }

        Map<Long, Long> confirmed = loadConfirmed(ids);
        Map<Long, Long> views = loadViews(ids);

        for (Event event : events) {
            event.setConfirmedRequests(confirmed.getOrDefault(event.getId(), 0L));
            event.setViews(views.getOrDefault(event.getId(), 0L));
        }
    }

    private Map<Long, Long> loadConfirmed(List<Long> eventIds) {
        Map<Long, Long> result = new HashMap<>();
        List<Object[]> rows = requestRepository.countConfirmedByEventIds(eventIds, RequestStatus.CONFIRMED);
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    private Map<Long, Long> loadViews(List<Long> eventIds) {
        Map<Long, Long> views = new HashMap<>();
        if (eventIds.isEmpty()) {
            return views;
        }

        List<String> uris = new ArrayList<>();
        for (Long id : eventIds) {
            uris.add("/events/" + id);
        }

        try {
            var response = statsClient.getStats(STATS_START, LocalDateTime.now(), uris, true);
            if (response.getBody() == null) {
                return views;
            }
            for (ViewStatDto stat : response.getBody()) {
                String uri = stat.getUri();
                long eventId = Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
                views.put(eventId, stat.getHits());
            }
        } catch (Exception ignored) {
            return views;
        }
        return views;
    }
}
