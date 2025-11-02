package org.smartsupply.SmartSupply.service;

import org.smartsupply.SmartSupply.dto.request.CategoryRequestDto;
import org.smartsupply.SmartSupply.dto.request.CategoryUpdateDto;
import org.smartsupply.SmartSupply.dto.response.CategoryDetailResponseDto;
import org.smartsupply.SmartSupply.dto.response.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto createCategory(CategoryRequestDto categoryRequestDto);
    List<CategoryResponseDto> getAllCategories();
    CategoryResponseDto getCategoryById(Long id);
    CategoryDetailResponseDto getCategoryWithProducts(Long id);
    CategoryResponseDto updateCategory(Long id , CategoryUpdateDto categoryUpdateDto);
    void deleteCategory(Long id);
    List<CategoryResponseDto> searchCategories(String name);
    boolean existsById(Long id);


}
