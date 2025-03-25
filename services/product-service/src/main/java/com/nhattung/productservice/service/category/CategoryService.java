package com.nhattung.productservice.service.category;

import com.nhattung.productservice.dto.CategoryDto;
import com.nhattung.productservice.entity.Category;
import com.nhattung.productservice.exception.AppException;
import com.nhattung.productservice.exception.ErrorCode;
import com.nhattung.productservice.repository.CategoryRepository;
import com.nhattung.productservice.request.CreateCategoryRequest;
import com.nhattung.productservice.request.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {


    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    @Override
    public Category saveCategory(CreateCategoryRequest createCategoryRequest) {
        Category category = Category.builder()
                .name(createCategoryRequest.getName())
                .build();
        return Optional.of(category)
                .filter(c -> !categoryRepository.existsByName(c.getName()))
                .map(categoryRepository::save)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_EXISTED));
    }

    @Override
    public Category updateCategory(UpdateCategoryRequest updateCategoryRequest, Long id) {
        return Optional.ofNullable(getCategoryById(id))
                .map(category -> {
                    category.setName(updateCategoryRequest.getName());
                    return categoryRepository.save(category);
                })
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                .ifPresentOrElse(categoryRepository::delete, () -> {
                    throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public List<Category> getCategoriesByName(String name) {
        return categoryRepository.findByNameContaining(name);
    }

    @Override
    public CategoryDto convertToDto(Category category) {
        return modelMapper.map(category, CategoryDto.class);
    }

    @Override
    public List<CategoryDto> convertToDtos(List<Category> categories) {
        return categories.stream()
                .map(this::convertToDto)
                .toList();
    }

}
