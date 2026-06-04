package ru.yandex.practicum.service.category;

import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(Long categoryId, CategoryDto dto);

    void deleteCategory(Long categoryId);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long categoryId);
}
