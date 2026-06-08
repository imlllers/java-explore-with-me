package ru.yandex.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.user.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.model.mapper.UserMapper;
import ru.yandex.practicum.repository.UserRepository;
import ru.yandex.practicum.service.rating.EventRatingService;
import ru.yandex.practicum.util.OffsetBasedPageRequest;
import ru.yandex.practicum.util.PageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EventRatingService eventRatingService;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(dto)));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<UserDto> users;
        if (ids != null && !ids.isEmpty()) {
            users = new ArrayList<>();
            for (User user : userRepository.findAllById(ids)) {
                users.add(UserMapper.toUserDto(user));
            }
            users = PageUtils.slice(users, from, size);
        } else {
            users = new ArrayList<>();
            for (User user : userRepository.findAll(new OffsetBasedPageRequest(from, size, Sort.by("id")))) {
                users.add(UserMapper.toUserDto(user));
            }
        }
        fillUserRatings(users);
        return users;
    }

    private void fillUserRatings(List<UserDto> users) {
        if (users.isEmpty()) {
            return;
        }
        List<Long> userIds = new ArrayList<>();
        for (UserDto user : users) {
            userIds.add(user.getId());
        }
        Map<Long, Long> ratings = eventRatingService.getUserRatings(userIds);
        for (UserDto user : users) {
            user.setRating(ratings.getOrDefault(user.getId(), 0L));
        }
    }
}
