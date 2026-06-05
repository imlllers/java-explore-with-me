package ru.yandex.practicum.dto.user;

import jakarta.validation.constraints.Email;
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
public class NewUserRequest {
    @NotBlank(message = "Email не должен быть пустым")
    @Email(message = "Некорректный email")
    @Size(min = 6, max = 254, message = "Длина email должна быть от {min} до {max} символов")
    private String email;

    @NotBlank(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 250, message = "Длина имени должна быть от {min} до {max} символов")
    private String name;
}
