package com.phelti.phelti.repository.jpa;

import com.phelti.phelti.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Integer> {

    List<CategoryEntity> findAllByOrderByNameAsc();
}
