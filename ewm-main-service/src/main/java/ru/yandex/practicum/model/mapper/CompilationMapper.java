package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CompilationMapper {

    public static CompilationDto toCompilationDto(Compilation compilation) {
        List<EventShortDto> events = new ArrayList<>();
        for (Event event : compilation.getEvents()) {
            events.add(EventMapper.toEventShortDto(event));
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}
