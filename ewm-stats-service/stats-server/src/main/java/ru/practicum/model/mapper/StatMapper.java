package ru.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.StatDto;
import ru.practicum.model.Stat;

@UtilityClass
public class StatMapper {
    public Stat toStat(StatDto dto) {
        return Stat.builder()
                .id(dto.getId())
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public StatDto toDto(Stat stat) {
        return StatDto.builder()
                .id(stat.getId())
                .app(stat.getApp())
                .uri(stat.getUri())
                .ip(stat.getIp())
                .timestamp(stat.getTimestamp())
                .build();
    }
}