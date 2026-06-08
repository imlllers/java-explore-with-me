package ru.yandex.practicum.service.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.repository.EventRatingRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventRatingService {
    private final EventRatingRepository eventRatingRepository;

    public void fillRatings(Event event) {
        List<Event> events = new ArrayList<>();
        events.add(event);
        fillRatings(events);
    }

    public void fillRatings(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Long> ids = new ArrayList<>();
        for (Event event : events) {
            ids.add(event.getId());
        }

        Map<Long, Long> ratings = loadRatings(ids);
        for (Event event : events) {
            event.setRating(ratings.getOrDefault(event.getId(), 0L));
        }
    }

    public Map<Long, Long> getUserRatings(List<Long> userIds) {
        Map<Long, Long> result = new HashMap<>();
        if (userIds.isEmpty()) {
            return result;
        }

        List<Object[]> rows = eventRatingRepository.sumRatesByInitiatorIds(userIds);
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    private Map<Long, Long> loadRatings(List<Long> eventIds) {
        Map<Long, Long> result = new HashMap<>();
        if (eventIds.isEmpty()) {
            return result;
        }

        List<Object[]> rows = eventRatingRepository.sumRatesByEventIds(eventIds);
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }
}
