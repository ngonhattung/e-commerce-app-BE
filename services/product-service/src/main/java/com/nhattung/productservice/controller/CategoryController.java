package com.nhattung.productservice.controller;

import com.nhattung.productservice.dto.CategoryDto;
import com.nhattung.productservice.entity.Category;
import com.nhattung.productservice.request.CreateCategoryRequest;
import com.nhattung.productservice.request.UpdateCategoryRequest;
import com.nhattung.productservice.response.ApiResponse;
import com.nhattung.productservice.service.category.ICategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ApiResponse<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = categoryService.saveCategory(request);
        CategoryDto categoryDto = categoryService.convertToDto(category);
        return ApiResponse.<CategoryDto>builder()
                .message("Category created successfully")
                .result(categoryDto)
                .build();
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ApiResponse<CategoryDto> updateCategory(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateCategoryRequest request) {
        Category category = categoryService.updateCategory(request, id);
        CategoryDto categoryDto = categoryService.convertToDto(category);
        return ApiResponse.<CategoryDto>builder()
                .message("Category updated successfully")
                .result(categoryDto)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }

    @GetMapping("category/{id}")
    public ApiResponse<CategoryDto> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        CategoryDto categoryDto = categoryService.convertToDto(category);
        return ApiResponse.<CategoryDto>builder()
                .message("Category fetched successfully")
                .result(categoryDto)
                .build();
    }

    @GetMapping("category/name/{name}")
    public ApiResponse<CategoryDto> getCategoryByName(@PathVariable String name) {
        Category category = categoryService.getCategoryByName(name);
        CategoryDto categoryDto = categoryService.convertToDto(category);
        return ApiResponse.<CategoryDto>builder()
                .message("Category fetched successfully")
                .result(categoryDto)
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDtos = categoryService.convertToDtos(categories);
        return ApiResponse.<List<CategoryDto>>builder()
                .message("Categories fetched successfully")
                .result(categoryDtos)
                .build();
    }

}
