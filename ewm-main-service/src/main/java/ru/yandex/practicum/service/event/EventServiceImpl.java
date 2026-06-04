package ru.yandex.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.event.NewEventDto;
import ru.yandex.practicum.dto.event.UpdateEventAdminRequest;
import ru.yandex.practicum.dto.event.UpdateEventUserRequest;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.enums.EventState;
import ru.yandex.practicum.model.mapper.EventMapper;
import ru.yandex.practicum.repository.CategoryRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.util.OffsetBasedPageRequest;
import ru.yandex.practicum.util.PageUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventStatService eventStatService;

    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        List<Long> userFilter = toFilter(users);
        List<Long> categoryFilter = toFilter(categories);
        List<EventState> stateFilter = toStateFilter(states);

        Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by("id"));
        Page<Event> page = eventRepository.findAdminEvents(userFilter, stateFilter, categoryFilter,
                rangeStart, rangeEnd, pageable);

        List<Event> events = page.getContent();
        eventStatService.fillStats(events);
        return EventMapper.toEventFullDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEvent(eventId);
        updateEventFields(event, request);
        if (request.getStateAction() != null) {
            updateAdminState(event, request.getStateAction());
        }
        Event saved = eventRepository.save(event);
        eventStatService.fillStats(saved);
        return EventMapper.toEventFullDto(saved);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        verifyUser(userId);
        Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by("id"));
        List<Event> events = eventRepository.findByInitiator_Id(userId, pageable).getContent();
        eventStatService.fillStats(events);
        return EventMapper.toEventShortDtos(events);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {
        User user = getUser(userId);
        Category category = getCategory(dto.getCategory());
        checkEventDate(dto.getEventDate());

        Event event = EventMapper.toEvent(dto, category, user, EventMapper.toLocation(dto.getLocation()));
        Event saved = eventRepository.save(event);
        eventStatService.fillStats(saved);
        return EventMapper.toEventFullDto(saved);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = getUserEventEntity(userId, eventId);
        eventStatService.fillStats(event);
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getUserEventEntity(userId, eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Изменять можно только события в статусе ожидания или отменённые");
        }

        updateEventFields(event, request);
        if (request.getEventDate() != null) {
            checkEventDate(request.getEventDate());
        }
        if (request.getStateAction() != null) {
            updateUserState(event, request.getStateAction());
        }

        Event saved = eventRepository.save(event);
        eventStatService.fillStats(saved);
        return EventMapper.toEventFullDto(saved);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort, int from, int size) {
        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        List<Long> categoryFilter = toFilter(categories);
        String textFilter = (text == null || text.isBlank()) ? null : text;
        boolean sortByViews = "VIEWS".equalsIgnoreCase(sort);

        if (!Boolean.TRUE.equals(onlyAvailable) && !sortByViews) {
            Pageable pageable = new OffsetBasedPageRequest(from, size, Sort.by("eventDate"));
            List<Event> events = eventRepository.findPublicEvents(textFilter, categoryFilter, paid, start, rangeEnd, pageable)
                    .getContent();
            eventStatService.fillStats(events);
            return EventMapper.toEventShortDtos(events);
        }

        List<Event> events = eventRepository.findAllPublicEvents(textFilter, categoryFilter, paid, start, rangeEnd);
        eventStatService.fillStats(events);

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = filterAvailable(events);
        }
        if (sortByViews) {
            sortByViews(events);
        }

        List<EventShortDto> result = EventMapper.toEventShortDtos(events);
        return PageUtils.slice(result, from, size);
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId) {
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        eventStatService.fillStats(event);
        return EventMapper.toEventFullDto(event);
    }

    private void updateEventFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(EventMapper.toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateEventFields(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(getCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(EventMapper.toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateAdminState(Event event, String action) {
        if ("PUBLISH_EVENT".equals(action)) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Невозможно опубликовать событие, так как оно не в нужном состоянии: " + event.getState());
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата события должна быть минимум через час после публикации");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if ("REJECT_EVENT".equals(action)) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Невозможно отклонить опубликованное событие");
            }
            event.setState(EventState.CANCELED);
        }
    }

    private void updateUserState(Event event, String action) {
        if ("SEND_TO_REVIEW".equals(action)) {
            event.setState(EventState.PENDING);
        } else if ("CANCEL_REVIEW".equals(action)) {
            event.setState(EventState.CANCELED);
        }
    }

    private List<Event> filterAvailable(List<Event> events) {
        List<Event> result = new ArrayList<>();
        for (Event event : events) {
            int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();
            long confirmed = event.getConfirmedRequests() == null ? 0 : event.getConfirmedRequests();
            if (limit == 0 || confirmed < limit) {
                result.add(event);
            }
        }
        return result;
    }

    private void sortByViews(List<Event> events) {
        for (int i = 0; i < events.size() - 1; i++) {
            for (int j = i + 1; j < events.size(); j++) {
                long viewsI = events.get(i).getViews() == null ? 0 : events.get(i).getViews();
                long viewsJ = events.get(j).getViews() == null ? 0 : events.get(j).getViews();
                if (viewsJ > viewsI) {
                    Event temp = events.get(i);
                    events.set(i, events.get(j));
                    events.set(j, temp);
                }
            }
        }
    }

    private List<Long> toFilter(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids;
    }

    private List<EventState> toStateFilter(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        List<EventState> result = new ArrayList<>();
        for (String state : states) {
            result.add(EventState.valueOf(state.toUpperCase()));
        }
        return result;
    }

    private void checkEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть минимум через два часа от текущего момента");
        }
    }

    private Event getEvent(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return optionalEvent.get();
    }

    private Event getUserEventEntity(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие с id=" + eventId + " не найдено");
        }
        return event;
    }

    private User getUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return optionalUser.get();
    }

    private Category getCategory(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new NotFoundException("Категория с id=" + categoryId + " не найдена");
        }
        return optionalCategory.get();
    }

    private void verifyUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
