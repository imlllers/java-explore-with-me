package ru.yandex.practicum.service.compilation;

import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto dto);
    CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest dto);
    void deleteCompilation(Long compilationId);
    CompilationDto getCompilation(Long compilationId);
    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}
