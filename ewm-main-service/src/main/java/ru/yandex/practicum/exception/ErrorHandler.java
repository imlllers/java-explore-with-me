package ru.yandex.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), List.of(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(ConflictException e) {
        return build(HttpStatus.CONFLICT, e.getMessage(), List.of(e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(ForbiddenException e) {
        return build(HttpStatus.FORBIDDEN, e.getMessage(), List.of(e.getMessage()));
    }

    @ExceptionHandler({ValidationException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(RuntimeException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), List.of(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(ErrorHandler::formatFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrity(DataIntegrityViolationException e) {
        String message = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        return build(HttpStatus.CONFLICT, message, List.of(message));
    }

    private static String formatFieldError(FieldError error) {
        return String.format("Поле: %s. Ошибка: %s. Значение: %s",
                error.getField(), error.getDefaultMessage(), error.getRejectedValue());
    }

    private ErrorResponse build(HttpStatus status, String message, List<String> errors) {
        log.warn("Обработка исключения: {}", message);
        return ErrorResponse.builder()
                .status(status.name())
                .reason(status.getReasonPhrase())
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
