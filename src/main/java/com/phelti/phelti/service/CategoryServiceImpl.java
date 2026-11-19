package com.phelti.phelti.service;

import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.exception.CategoryAlreadyExistsException;
import com.phelti.phelti.exception.InvalidCategoryDataException;
import com.phelti.phelti.repository.jpa.CategoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_NAME_LENGTH = 80;

    private final CategoryJpaRepository categoryRepository;

    public CategoryServiceImpl(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryEntity createCategory(String name) {

        if (name == null || name.isBlank()) {
            throw new InvalidCategoryDataException("name must not be empty");
        }

        String cleanName = name.trim();

        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new InvalidCategoryDataException(
                    "name must be at most " + MAX_NAME_LENGTH + " characters"
            );
        }

        if (categoryRepository.existsByNameIgnoreCase(cleanName)) {
            throw new CategoryAlreadyExistsException(cleanName);
        }

        CategoryEntity category = new CategoryEntity();
        category.setName(cleanName);

        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryEntity> findAllCategories() {

        return categoryRepository.findAllByOrderByNameAsc();
    }
}
