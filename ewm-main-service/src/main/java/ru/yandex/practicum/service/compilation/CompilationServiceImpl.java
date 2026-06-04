package ru.yandex.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.dto.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Compilation;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.mapper.CompilationMapper;
import ru.yandex.practicum.repository.CompilationRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.service.event.EventStatService;
import ru.yandex.practicum.util.OffsetBasedPageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventStatService eventStatService;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(dto.getTitle());
        compilation.setPinned(dto.getPinned() != null && dto.getPinned());
        compilation.setEvents(new HashSet<>(findEvents(dto.getEvents())));
        return toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest dto) {
        Compilation compilation = getCompilationEntity(compilationId);
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            compilation.setEvents(new HashSet<>(findEvents(dto.getEvents())));
        }
        return toDto(compilationRepository.save(compilation));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException("Подборка с id=" + compilationId + " не найдена");
        }
        compilationRepository.deleteById(compilationId);
    }

    @Override
    public CompilationDto getCompilation(Long compilationId) {
        return toDto(getCompilationEntity(compilationId));
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by("id"));
        Page<Compilation> page;
        if (pinned == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            page = compilationRepository.findByPinned(pinned, pageable);
        }

        List<CompilationDto> result = new ArrayList<>();
        for (Compilation compilation : page.getContent()) {
            result.add(toDto(compilation));
        }
        return result;
    }

    private CompilationDto toDto(Compilation compilation) {
        List<Event> events = new ArrayList<>(compilation.getEvents());
        eventStatService.fillStats(events);
        return CompilationMapper.toCompilationDto(compilation);
    }

    private Set<Event> findEvents(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        Set<Event> events = new HashSet<>(eventRepository.findAllById(ids));
        for (Long id : ids) {
            boolean found = false;
            for (Event event : events) {
                if (event.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new NotFoundException("Событие с id=" + id + " не найдено");
            }
        }
        return events;
    }

    private Compilation getCompilationEntity(Long compilationId) {
        Optional<Compilation> optional = compilationRepository.findById(compilationId);
        if (optional.isEmpty()) {
            throw new NotFoundException("Подборка с id=" + compilationId + " не найдена");
        }
        return optional.get();
    }
}
