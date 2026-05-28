package ru.practicum.service;

import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    void saveHit(StatDto dto);

    List<ViewStatDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    );
}