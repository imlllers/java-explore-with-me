package ru.yandex.practicum.model.mapper;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.dto.LocationDto;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.event.EventFullDto;
import ru.yandex.practicum.dto.event.EventShortDto;
import ru.yandex.practicum.dto.event.NewEventDto;
import ru.yandex.practicum.dto.user.UserShortDto;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.Event;
import ru.yandex.practicum.model.Location;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.enums.EventState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class EventMapper {

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .eventDate(event.getEventDate())
                .title(event.getTitle())
                .paid(event.getPaid())
                .category(toCategoryDto(event.getCategory()))
                .initiator(toUserShortDto(event.getInitiator()))
                .confirmedRequests(event.getConfirmedRequests())
                .views(event.getViews())
                .build();
    }

    public static List<EventShortDto> toEventShortDtos(List<Event> events) {
        List<EventShortDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventShortDto(event));
        }
        return result;
    }

    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .eventDate(event.getEventDate())
                .initiator(toUserShortDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState() != null ? event.getState().name() : null)
                .publishedOn(event.getPublishedOn())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static List<EventFullDto> toEventFullDtos(List<Event> events) {
        List<EventFullDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(toEventFullDto(event));
        }
        return result;
    }

    public static Event toEvent(NewEventDto dto, Category category, User initiator, Location location) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .createdOn(LocalDateTime.now())
                .eventDate(dto.getEventDate())
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(EventState.PENDING)
                .title(dto.getTitle())
                .category(category)
                .initiator(initiator)
                .location(location)
                .build();
    }

    public static Location toLocation(LocationDto dto) {
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }

    public static LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryMapper.toCategoryDto(category);
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserMapper.toUserShortDto(user);
    }
}
