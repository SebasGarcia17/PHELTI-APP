package com.phelti.phelti.service;

import com.phelti.phelti.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {

    CategoryEntity createCategory(String name);

    List<CategoryEntity> findAllCategories();
}
