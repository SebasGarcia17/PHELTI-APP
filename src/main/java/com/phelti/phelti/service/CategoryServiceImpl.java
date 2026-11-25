package com.phelti.phelti.service;

import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.repository.jpa.CategoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryJpaRepository categoryRepository;

    public CategoryServiceImpl(CategoryJpaRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryEntity> findAllCategories() {

        return categoryRepository.findAllByOrderByNameAsc();
    }
}
