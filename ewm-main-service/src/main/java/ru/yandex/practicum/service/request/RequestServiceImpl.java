package ru.yandex.practicum.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.yandex.practicum.dto.request.ParticipationRequestDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.ParticipationRequest;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.enums.EventState;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.model.mapper.RequestMapper;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.repository.RequestRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        verifyUser(userId);
        List<ParticipationRequestDto> result = new ArrayList<>();
        for (ParticipationRequest request : requestRepository.findByRequester_Id(userId)) {
            result.add(RequestMapper.toDto(request));
        }
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Инициатор не может подать заявку на своё событие");
        }
        if (requestRepository.existsByRequester_IdAndEvent_Id(userId, eventId)) {
            throw new ConflictException("Повторная заявка");
        }

        long confirmedCount = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else if (event.getRequestModeration() != null && !event.getRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(status);

        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = getUserRequest(userId, requestId);
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        getUserEvent(userId, eventId);
        List<ParticipationRequestDto> result = new ArrayList<>();
        for (ParticipationRequest request : requestRepository.findByEvent_Id(eventId)) {
            result.add(RequestMapper.toDto(request));
        }
        return result;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest requestDto) {
        Event event = getUserEvent(userId, eventId);
        if (requestDto.getRequestIds() == null || requestDto.getRequestIds().isEmpty()) {
            throw new ConflictException("Не указаны id заявок");
        }

        RequestStatus targetStatus = parseStatus(requestDto.getStatus());
        if (targetStatus == RequestStatus.PENDING) {
            throw new ConflictException("Нельзя изменить статус на PENDING");
        }

        List<ParticipationRequest> requests = new ArrayList<>();
        for (ParticipationRequest request : requestRepository.findAllById(requestDto.getRequestIds())) {
            if (request.getEvent().getId().equals(eventId)) {
                requests.add(request);
            }
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        long confirmedCount = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Заявка должна иметь статус PENDING");
            }
            if (targetStatus == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
                    throw new ConflictException("Достигнут лимит участников");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmed.add(RequestMapper.toDto(request));
            } else {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(request));
            }
        }
        requestRepository.saveAll(requests);

        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            List<ParticipationRequest> pending = requestRepository.findByEvent_IdAndStatus(eventId, RequestStatus.PENDING);
            for (ParticipationRequest request : pending) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toDto(request));
            }
            requestRepository.saveAll(pending);
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);
        return result;
    }

    private RequestStatus parseStatus(String status) {
        try {
            return RequestStatus.valueOf(status);
        } catch (Exception e) {
            throw new ConflictException("Неизвестный статус: " + status);
        }
    }

    private User getUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return optionalUser.get();
    }

    private Event getEvent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return optionalEvent.get();
    }

    private Event getUserEvent(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return event;
    }

    private ParticipationRequest getUserRequest(Long userId, Long requestId) {
        Optional<ParticipationRequest> optionalRequest = requestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new NotFoundException("Заявка с id=" + requestId + " не найдена");
        }
        ParticipationRequest request = optionalRequest.get();
        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Заявка с id=" + requestId + " не найдена");
        }
        return request;
    }

    private void verifyUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
