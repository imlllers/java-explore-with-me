package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.util.DateTimePattern;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShortDto {
    private Long id;
    private String annotation;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime eventDate;

    private String title;
    private Boolean paid;
    private CategoryDto category;
    private UserShortDto initiator;
    private Long confirmedRequests;
    private Long views;
}
