package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.model.ParticipationRequest;
import ru.yandex.practicum.model.enums.RequestStatus;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByRequester_IdAndEvent_Id(Long requesterId, Long eventId);

    boolean existsByRequester_IdAndEvent_IdAndStatus(Long requesterId, Long eventId, RequestStatus status);

    long countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByEvent_Id(Long eventId);

    List<ParticipationRequest> findByRequester_Id(Long requesterId);

    List<ParticipationRequest> findByEvent_IdAndStatus(Long eventId, RequestStatus status);

    @Query("""
            SELECT r.event.id, COUNT(r)
            FROM ParticipationRequest r
            WHERE r.event.id IN :eventIds AND r.status = :status
            GROUP BY r.event.id
            """)
    List<Object[]> countConfirmedByEventIds(@Param("eventIds") Collection<Long> eventIds,
                                          @Param("status") RequestStatus status);
}
