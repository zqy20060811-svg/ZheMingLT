package com.zheminglt.controller;

import com.zheminglt.service.CategoryService;
import com.zheminglt.vo.CategoryVO;
import com.zheminglt.vo.ResponseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "分类管理", description = "帖子分类的增删改查接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取所有分类", description = "获取所有帖子分类列表")
    @GetMapping
    public ResponseVO<List<CategoryVO>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取单个分类的详细信息")
    @GetMapping("/{id}")
    public ResponseVO<CategoryVO> getCategoryById(@Parameter(description = "分类ID") @PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @Operation(summary = "创建分类", description = "创建新分类，需要管理员权限")
    @SecurityRequirement(name = "Authorization")
    @PostMapping
    public ResponseVO<CategoryVO> createCategory(@RequestBody CategoryVO categoryVO) {
        return categoryService.createCategory(categoryVO);
    }

    @Operation(summary = "更新分类", description = "更新分类信息，需要管理员权限")
    @SecurityRequirement(name = "Authorization")
    @PutMapping("/{id}")
    public ResponseVO<CategoryVO> updateCategory(@Parameter(description = "分类ID") @PathVariable Long id, 
                                                  @RequestBody CategoryVO categoryVO) {
        return categoryService.updateCategory(id, categoryVO);
    }

    @Operation(summary = "删除分类", description = "删除分类，需要管理员权限")
    @SecurityRequirement(name = "Authorization")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deleteCategory(@Parameter(description = "分类ID") @PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
