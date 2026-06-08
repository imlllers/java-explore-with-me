package ru.yandex.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.service.rating.RatingService;

@RestController
@RequiredArgsConstructor
public class PrivateRatingController {
    private final RatingService ratingService;

    @PatchMapping("/users/{userId}/events/{eventId}/like")
    public void addLike(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.addLike(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/dislike")
    public void addDislike(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.addDislike(userId, eventId);
    }

    @DeleteMapping("/users/{userId}/events/{eventId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLike(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.removeLike(userId, eventId);
    }

    @DeleteMapping("/users/{userId}/events/{eventId}/dislike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDislike(@PathVariable Long userId, @PathVariable Long eventId) {
        ratingService.removeDislike(userId, eventId);
    }
}
