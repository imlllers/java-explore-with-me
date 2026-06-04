package ru.yandex.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.dto.LocationDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewEventDto {
    @NotBlank(message = "Аннотация не должна быть пустой")
    @Size(min = 20, max = 2000, message = "Длина аннотации должна быть от {min} до {max} символов")
    private String annotation;

    @NotNull(message = "Категория должна быть указана")
    private Long category;

    @NotBlank(message = "Описание не должно быть пустым")
    @Size(min = 20, max = 7000, message = "Длина описания должна быть от {min} до {max} символов")
    private String description;

    @NotNull(message = "Дата события должна быть указана")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Местоположение должно быть указано")
    private LocationDto location;

    private Boolean paid = false;
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;

    @NotBlank(message = "Заголовок не должен быть пустым")
    @Size(min = 3, max = 120, message = "Длина заголовка должна быть от {min} до {max} символов")
    private String title;
}
