package com.zheminglt.impl;

import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.mapper.CategoryMapper;
import com.zheminglt.model.Category;
import com.zheminglt.service.CategoryService;
import com.zheminglt.vo.CategoryVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ResponseVO<List<CategoryVO>> getAllCategories() {
        List<Category> categories = categoryMapper.findAll();
        List<CategoryVO> categoryVOs = categories.stream()
                .map(category -> {
                    CategoryVO categoryVO = new CategoryVO();
                    BeanUtils.copyProperties(category, categoryVO);
                    return categoryVO;
                })
                .collect(Collectors.toList());
        return ResponseVO.success(categoryVOs);
    }

    @Override
    public ResponseVO<CategoryVO> getCategoryById(Long id) {
        Category category = categoryMapper.findById(id).orElse(null);
        if (category == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.CATEGORY_NOT_FOUND);
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return ResponseVO.success(categoryVO);
    }

    @Override
    public ResponseVO<CategoryVO> createCategory(CategoryVO categoryVO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryVO, category);
        Category savedCategory = categoryMapper.save(category);
        CategoryVO savedCategoryVO = new CategoryVO();
        BeanUtils.copyProperties(savedCategory, savedCategoryVO);
        return ResponseVO.success(savedCategoryVO);
    }

    @Override
    public ResponseVO<CategoryVO> updateCategory(Long id, CategoryVO categoryVO) {
        Category existingCategory = categoryMapper.findById(id).orElse(null);
        if (existingCategory == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.CATEGORY_NOT_FOUND);
        }
        BeanUtils.copyProperties(categoryVO, existingCategory);
        Category savedCategory = categoryMapper.save(existingCategory);
        CategoryVO savedCategoryVO = new CategoryVO();
        BeanUtils.copyProperties(savedCategory, savedCategoryVO);
        return ResponseVO.success(savedCategoryVO);
    }

    @Override
    public ResponseVO<Void> deleteCategory(Long id) {
        Category category = categoryMapper.findById(id).orElse(null);
        if (category == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.CATEGORY_NOT_FOUND);
        }
        categoryMapper.delete(category);
        return ResponseVO.success(null);
    }
}
