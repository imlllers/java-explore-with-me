package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.EventRating;

import java.util.Collection;
import java.util.List;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    void deleteByUserIdAndEventIdAndRate(Long userId, Long eventId, Integer rate);

    @Query("""
            SELECT r.eventId, SUM(r.rate)
            FROM EventRating r
            WHERE r.eventId IN :eventIds
            GROUP BY r.eventId
            """)
    List<Object[]> sumRatesByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("""
            SELECT e.initiator.id, SUM(r.rate)
            FROM EventRating r, Event e
            WHERE r.eventId = e.id AND e.initiator.id IN :userIds
            GROUP BY e.initiator.id
            """)
    List<Object[]> sumRatesByInitiatorIds(@Param("userIds") Collection<Long> userIds);
}
