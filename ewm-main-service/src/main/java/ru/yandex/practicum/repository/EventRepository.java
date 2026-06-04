package ru.yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Override
    Optional<Event> findById(Long id);

    boolean existsByCategory_Id(Long categoryId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Page<Event> findByInitiator_Id(Long initiatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query(value = """
            SELECT e FROM Event e
            WHERE (:users IS NULL OR e.initiator.id IN :users)
            AND (:states IS NULL OR e.state IN :states)
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart)
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            """,
            countQuery = """
            SELECT count(e) FROM Event e
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

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query(value = """
            SELECT e FROM Event e
            WHERE e.state = :state
            AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%'))
                 OR lower(e.description) LIKE lower(concat('%', :text, '%')))
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            """,
            countQuery = """
            SELECT count(e) FROM Event e
            WHERE e.state = :state
            AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%'))
                 OR lower(e.description) LIKE lower(concat('%', :text, '%')))
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            """)
    Page<Event> findPublicEvents(@Param("state") EventState state,
                                 @Param("text") String text,
                                 @Param("categories") List<Long> categories,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("""
            SELECT e FROM Event e
            WHERE e.state = :state
            AND (:text IS NULL OR lower(e.annotation) LIKE lower(concat('%', :text, '%'))
                 OR lower(e.description) LIKE lower(concat('%', :text, '%')))
            AND (:categories IS NULL OR e.category.id IN :categories)
            AND (:paid IS NULL OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findAllPublicEvents(@Param("state") EventState state,
                                    @Param("text") String text,
                                    @Param("categories") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("rangeEnd") LocalDateTime rangeEnd);
}
