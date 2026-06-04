package ru.yandex.practicum.service.event;

import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.event.NewEventDto;
import ru.yandex.practicum.dto.event.UpdateEventAdminRequest;
import ru.yandex.practicum.dto.event.UpdateEventUserRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);
    List<EventShortDto> getUserEvents(Long userId, int from, int size);
    EventFullDto addEvent(Long userId, NewEventDto dto);
    EventFullDto getUserEvent(Long userId, Long eventId);
    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                       Boolean onlyAvailable, String sort, int from, int size);
    EventFullDto getPublicEventById(Long eventId);
}
