package ru.yandex.practicum.service.rating;

public interface RatingService {
    void addLike(Long userId, Long eventId);

    void addDislike(Long userId, Long eventId);

    void removeLike(Long userId, Long eventId);

    void removeDislike(Long userId, Long eventId);
}
