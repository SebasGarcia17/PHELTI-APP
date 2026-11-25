package com.phelti.phelti.service;

import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.repository.jpa.CategoryJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryJpaRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findAllCategoriesShouldReturnRepositoryCategories() {

        List<CategoryEntity> categories = List.of(new CategoryEntity(), new CategoryEntity());

        when(categoryRepository.findAllByOrderByNameAsc())
                .thenReturn(categories);

        assertEquals(categories, categoryService.findAllCategories());
    }
}
