package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.util.DateTimePattern;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime eventDate;

    private LocationDto location;
    private Boolean paid;

    @Min(0)
    private Integer participantLimit;

    private Boolean requestModeration;
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
