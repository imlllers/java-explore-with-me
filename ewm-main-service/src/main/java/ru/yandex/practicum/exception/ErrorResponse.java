package ru.yandex.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.util.DateTimePattern;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String status;
    private String reason;
    private String message;
    private List<String> errors;

    @JsonFormat(pattern = DateTimePattern.PATTERN)
    private LocalDateTime timestamp;
}
