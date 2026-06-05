package ru.yandex.practicum.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Название категории не должно быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории должна быть от {min} до {max} символов")
    private String name;
}
