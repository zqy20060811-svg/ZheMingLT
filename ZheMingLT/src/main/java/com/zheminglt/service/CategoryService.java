package com.zheminglt.service;

import com.zheminglt.vo.CategoryVO;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

public interface CategoryService {
    ResponseVO<List<CategoryVO>> getAllCategories();
    ResponseVO<CategoryVO> getCategoryById(Long id);
    ResponseVO<CategoryVO> createCategory(CategoryVO categoryVO);
    ResponseVO<CategoryVO> updateCategory(Long id, CategoryVO categoryVO);
    ResponseVO<Void> deleteCategory(Long id);
}