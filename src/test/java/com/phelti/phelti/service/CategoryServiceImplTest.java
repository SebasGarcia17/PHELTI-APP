package com.phelti.phelti.service;

import com.phelti.phelti.entity.CategoryEntity;
import com.phelti.phelti.exception.CategoryAlreadyExistsException;
import com.phelti.phelti.exception.InvalidCategoryDataException;
import com.phelti.phelti.repository.jpa.CategoryJpaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryJpaRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategoryShouldSaveTrimmedNameWhenDataIsValid() {

        when(categoryRepository.existsByNameIgnoreCase("Musica"))
                .thenReturn(false);

        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryEntity result = categoryService.createCategory("  Musica  ");

        assertEquals("Musica", result.getName());
        verify(categoryRepository).save(result);
    }

    @Test
    void createCategoryShouldThrowExceptionWhenNameIsBlank() {

        assertThrows(
                InvalidCategoryDataException.class,
                () -> categoryService.createCategory("  ")
        );

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void createCategoryShouldThrowExceptionWhenNameIsTooLong() {

        String longName = "x".repeat(81);

        assertThrows(
                InvalidCategoryDataException.class,
                () -> categoryService.createCategory(longName)
        );
    }

    @Test
    void createCategoryShouldThrowExceptionWhenCategoryAlreadyExists() {

        when(categoryRepository.existsByNameIgnoreCase("Musica"))
                .thenReturn(true);

        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.createCategory("Musica")
        );

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void findAllCategoriesShouldReturnRepositoryCategories() {

        List<CategoryEntity> categories = List.of(new CategoryEntity(), new CategoryEntity());

        when(categoryRepository.findAllByOrderByNameAsc())
                .thenReturn(categories);

        assertEquals(categories, categoryService.findAllCategories());
    }
}
