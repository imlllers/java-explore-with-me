package ru.yandex.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.UpdateEventAdminRequest;
import ru.yandex.practicum.service.event.EventService;
import ru.yandex.practicum.util.DateTimePattern;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DateTimePattern.PATTERN) LocalDateTime rangeStart,
                                        @RequestParam(required = false)
                                        @DateTimeFormat(pattern = DateTimePattern.PATTERN) LocalDateTime rangeEnd,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequest dto) {
        return eventService.updateEventByAdmin(eventId, dto);
    }
}
