package com.zheminglt.mapper;

import com.zheminglt.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryMapper extends JpaRepository<Category, Long> {
    Category findByName(String name);
}