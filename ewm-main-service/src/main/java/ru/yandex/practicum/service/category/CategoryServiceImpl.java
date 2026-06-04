package ru.yandex.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.category.CategoryDto;
import ru.yandex.practicum.dto.category.NewCategoryDto;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.model.Category;
import ru.yandex.practicum.model.mapper.CategoryMapper;
import ru.yandex.practicum.repository.CategoryRepository;
import ru.yandex.practicum.repository.EventRepository;
import ru.yandex.practicum.util.OffsetBasedPageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto dto) {
        Category category = CategoryMapper.toCategory(dto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto dto) {
        Category category = getCategoryEntity(categoryId);
        if (!category.getName().equals(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }
        category.setName(dto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория с id=" + categoryId + " не найдена");
        }
        if (eventRepository.existsByCategory_Id(categoryId)) {
            throw new ConflictException("Категория не пустая");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        List<CategoryDto> result = new ArrayList<>();
        for (Category category : categoryRepository.findAll(new OffsetBasedPageRequest(from, size, Sort.by("id")))) {
            result.add(CategoryMapper.toCategoryDto(category));
        }
        return result;
    }

    @Override
    public CategoryDto getCategory(Long categoryId) {
        return CategoryMapper.toCategoryDto(getCategoryEntity(categoryId));
    }

    private Category getCategoryEntity(Long categoryId) {
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isEmpty()) {
            throw new NotFoundException("Категория с id=" + categoryId + " не найдена");
        }
        return optionalCategory.get();
    }
}
