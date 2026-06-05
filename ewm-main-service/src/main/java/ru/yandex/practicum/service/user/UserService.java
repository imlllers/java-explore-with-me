package ru.yandex.practicum.service.user;

import ru.yandex.practicum.dto.user.NewUserRequest;
import ru.yandex.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest dto);

    void deleteUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
