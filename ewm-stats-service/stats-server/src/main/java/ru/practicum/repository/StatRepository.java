package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.ViewStatDto;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<Stat, Long> {
    @Query("""
            SELECT new ru.practicum.dto.ViewStatDto(s.app, s.uri, COUNT(s.id))
            FROM Stat s
            WHERE s.timestamp BETWEEN :start AND :end
            AND (:uris IS NULL OR s.uri IN :uris)
            GROUP BY s.app, s.uri
            ORDER BY COUNT(s.id) DESC
            """)
    List<ViewStatDto> findStats(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("uris") List<String> uris);

    @Query("""
            SELECT new ru.practicum.dto.ViewStatDto(s.app, s.uri, COUNT(DISTINCT s.ip))
            FROM Stat s
            WHERE s.timestamp BETWEEN :start AND :end
            AND (:uris IS NULL OR s.uri IN :uris)
            GROUP BY s.app, s.uri
            ORDER BY COUNT(DISTINCT s.ip) DESC
            """)
    List<ViewStatDto> findUniqueStats(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}