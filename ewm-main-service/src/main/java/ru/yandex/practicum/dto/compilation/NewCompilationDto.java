package ru.yandex.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCompilationDto {
    private Set<Long> events;
    private Boolean pinned = false;

    @NotBlank(message = "Название подборки не должно быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия подборки должна быть от {min} до {max} символов")
    private String title;
}
