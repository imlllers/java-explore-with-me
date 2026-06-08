package ru.yandex.practicum.service.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.ForbiddenException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.EventRating;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.enums.EventState;
import ru.yandex.practicum.model.enums.RequestStatus;
import ru.yandex.practicum.repository.EventRatingRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.repository.RequestRepository;
import ru.yandex.practicum.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RatingServiceImpl implements RatingService {
    private static final int LIKE = 1;
    private static final int DISLIKE = -1;

    private final EventRatingRepository eventRatingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Override
    @Transactional
    public void addLike(Long userId, Long eventId) {
        addRate(userId, eventId, LIKE);
    }

    @Override
    @Transactional
    public void addDislike(Long userId, Long eventId) {
        addRate(userId, eventId, DISLIKE);
    }

    @Override
    @Transactional
    public void removeLike(Long userId, Long eventId) {
        removeRate(userId, eventId, LIKE);
    }

    @Override
    @Transactional
    public void removeDislike(Long userId, Long eventId) {
        removeRate(userId, eventId, DISLIKE);
    }

    private void addRate(Long userId, Long eventId, int rate) {
        getUser(userId);
        Event event = getPublishedEvent(eventId);
        checkRater(userId, event);

        if (eventRatingRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new ConflictException("Пользователь уже оценил это событие");
        }

        EventRating eventRating = EventRating.builder()
                .userId(userId)
                .eventId(eventId)
                .rate(rate)
                .build();
        eventRatingRepository.save(eventRating);
    }

    private void removeRate(Long userId, Long eventId, int rate) {
        getUser(userId);
        getPublishedEvent(eventId);
        eventRatingRepository.deleteByUserIdAndEventIdAndRate(userId, eventId, rate);
    }

    private void checkRater(Long userId, Event event) {
        if (event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Инициатор не может оценить своё событие");
        }
        if (!requestRepository.existsByRequester_IdAndEvent_IdAndStatus(userId, event.getId(), RequestStatus.CONFIRMED)) {
            throw new ForbiddenException("Оценить событие может только участник с подтверждённой заявкой");
        }
    }

    private User getUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return optionalUser.get();
    }

    private Event getPublishedEvent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        Event event = optionalEvent.get();
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return event;
    }
}
