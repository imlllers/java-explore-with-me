package ru.yandex.practicum.repository;

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
    List<Event> findByInitiator_Id(Long initiatorId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query("""
            SELECT e FROM Event e
            WHERE (:ignoreUsers = true OR e.initiator.id IN :users)
            AND (:ignoreStates = true OR e.state IN :states)
            AND (:ignoreCategories = true OR e.category.id IN :categories)
            AND (:ignoreRangeStart = true OR e.eventDate >= :rangeStart)
            AND (:ignoreRangeEnd = true OR e.eventDate <= :rangeEnd)
            """)
    List<Event> findAdminEvents(@Param("ignoreUsers") boolean ignoreUsers,
                                @Param("users") List<Long> users,
                                @Param("ignoreStates") boolean ignoreStates,
                                @Param("states") List<EventState> states,
                                @Param("ignoreCategories") boolean ignoreCategories,
                                @Param("categories") List<Long> categories,
                                @Param("ignoreRangeStart") boolean ignoreRangeStart,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("ignoreRangeEnd") boolean ignoreRangeEnd,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("""
            SELECT e FROM Event e
            WHERE e.state = :state
            AND (:ignoreText = true OR lower(e.annotation) LIKE :textPattern
                 OR lower(e.description) LIKE :textPattern)
            AND (:ignoreCategories = true OR e.category.id IN :categories)
            AND (:ignorePaid = true OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:ignoreRangeEnd = true OR e.eventDate <= :rangeEnd)
            """)
    List<Event> findPublicEvents(@Param("state") EventState state,
                                 @Param("ignoreText") boolean ignoreText,
                                 @Param("textPattern") String textPattern,
                                 @Param("ignoreCategories") boolean ignoreCategories,
                                 @Param("categories") List<Long> categories,
                                 @Param("ignorePaid") boolean ignorePaid,
                                 @Param("paid") Boolean paid,
                                 @Param("rangeStart") LocalDateTime rangeStart,
                                 @Param("ignoreRangeEnd") boolean ignoreRangeEnd,
                                 @Param("rangeEnd") LocalDateTime rangeEnd,
                                 Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    @Query("""
            SELECT e FROM Event e
            WHERE e.state = :state
            AND (:ignoreText = true OR lower(e.annotation) LIKE :textPattern
                 OR lower(e.description) LIKE :textPattern)
            AND (:ignoreCategories = true OR e.category.id IN :categories)
            AND (:ignorePaid = true OR e.paid = :paid)
            AND e.eventDate >= :rangeStart
            AND (:ignoreRangeEnd = true OR e.eventDate <= :rangeEnd)
            ORDER BY e.eventDate ASC
            """)
    List<Event> findAllPublicEvents(@Param("state") EventState state,
                                    @Param("ignoreText") boolean ignoreText,
                                    @Param("textPattern") String textPattern,
                                    @Param("ignoreCategories") boolean ignoreCategories,
                                    @Param("categories") List<Long> categories,
                                    @Param("ignorePaid") boolean ignorePaid,
                                    @Param("paid") Boolean paid,
                                    @Param("rangeStart") LocalDateTime rangeStart,
                                    @Param("ignoreRangeEnd") boolean ignoreRangeEnd,
                                    @Param("rangeEnd") LocalDateTime rangeEnd);
}
