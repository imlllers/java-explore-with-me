package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatDto;
import ru.practicum.dto.ViewStatDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Stat;
import ru.practicum.model.mapper.StatMapper;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    public void saveHit(StatDto dto) {
        Stat stat = StatMapper.toStat(dto);

        statRepository.save(stat);
    }

    @Override
    public List<ViewStatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала позже даты окончания");
        }

        return List.of();
    }
}