package com.nhattung.productservice.service.category;

import com.nhattung.productservice.dto.CategoryDto;
import com.nhattung.productservice.entity.Category;
import com.nhattung.productservice.request.CreateCategoryRequest;
import com.nhattung.productservice.request.UpdateCategoryRequest;

import java.util.List;

public interface ICategoryService {

    Category saveCategory(CreateCategoryRequest createCategoryRequest);
    Category updateCategory(UpdateCategoryRequest updateCategoryRequest, Long id);
    void deleteCategory(Long id);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
    Category getCategoryByName(String name);
    List<Category> getCategoriesByName(String name);
    CategoryDto convertToDto(Category category);
    List<CategoryDto> convertToDtos(List<Category> categories);

}
