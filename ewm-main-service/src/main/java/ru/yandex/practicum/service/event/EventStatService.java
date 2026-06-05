package ru.yandex.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.ViewStatDto;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.repository.RequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventStatService {
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
        LocalDateTime start = resolveStatsStart(events);
        LocalDateTime end = LocalDateTime.now();
        Map<Long, Long> views = loadViews(ids, start, end);

        for (Event event : events) {
            Long eventId = event.getId();
            event.setConfirmedRequests(confirmed.getOrDefault(eventId, 0L));
            event.setViews(views.getOrDefault(eventId, 0L));
        }
    }

    private Map<Long, Long> loadConfirmed(List<Long> eventIds) {
        Map<Long, Long> result = new HashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }
        List<Object[]> rows = requestRepository.countConfirmedByEventIds(eventIds, RequestStatus.CONFIRMED);
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    private Map<Long, Long> loadViews(List<Long> eventIds, LocalDateTime start, LocalDateTime end) {
        Map<Long, Long> views = new HashMap<>();
        if (eventIds.isEmpty()) {
            return views;
        }

        List<String> uris = new ArrayList<>();
        for (Long id : eventIds) {
            uris.add("/events/" + id);
        }

        try {
            var response = statsClient.getStats(start, end, uris, true);
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

    private LocalDateTime resolveStatsStart(List<Event> events) {
        LocalDateTime earliest = null;
        for (Event event : events) {
            LocalDateTime publishedOn = event.getPublishedOn();
            if (publishedOn == null) {
                continue;
            }
            if (earliest == null || publishedOn.isBefore(earliest)) {
                earliest = publishedOn;
            }
        }
        if (earliest == null) {
            return startOfCurrentYear();
        }
        return earliest;
    }

    private static LocalDateTime startOfCurrentYear() {
        return LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0);
    }
}
