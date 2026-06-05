package ru.yandex.practicum.util;

import java.util.List;

public final class PageUtils {
    private PageUtils() {
    }

    public static <T> List<T> slice(List<T> list, int from, int size) {
        if (from >= list.size()) {
            return List.of();
        }
        return list.subList(from, Math.min(from + size, list.size()));
    }
}
