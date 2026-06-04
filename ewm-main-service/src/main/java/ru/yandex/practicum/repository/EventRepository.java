package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByCategory_Id(Long categoryId);

    Page<Event> findByInitiator_Id(Long initiatorId, Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            WHERE (:users IS NULL OR e.initiator.id IN :users)
            AND (:states IS NULL OR e.state IN :states)
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            """)
    Page<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("states") List<EventState> states,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            WHERE e.state = ru.yandex.practicum.model.enums.EventState.PUBLISHED
            AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%'))
                 OR lower(e.description) LIKE lower(concat('%', :text, '%')))
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            """)
    Page<Event> findPublicEvents(@Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            WHERE e.state = ru.yandex.practicum.model.enums.EventState.PUBLISHED
            AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%'))
                 OR lower(e.description) LIKE lower(concat('%', :text, '%')))
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findAllPublicEvents(@Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd);
}
