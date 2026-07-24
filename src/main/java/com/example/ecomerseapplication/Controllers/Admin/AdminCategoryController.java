package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.UpdateCategoryRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactCategoryAdminResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactAdminCategoryProjection;
import com.example.ecomerseapplication.Mappers.CategoryMapper;
import com.example.ecomerseapplication.Services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/category/")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("all/compact")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getAllCategories() {
        List<CompactAdminCategoryProjection> projections = categoryService.findAllCompact();

        List<CompactCategoryAdminResponse> response = CategoryMapper.compactAdminProjListToResponseList(projections);

        return ResponseEntity.ok(response);
    }

    @GetMapping("detailed/{categoryId}")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> getDetailedCategory(@PathVariable Integer categoryId) {

        return ResponseEntity.ok(categoryService.getDetailedCategory(categoryId));
    }

    @PatchMapping("update")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> updateCategory(@RequestBody @Valid UpdateCategoryRequest request) {

        categoryService.updateCategory(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("create")
    @PreAuthorize("hasRole(@roles.admin())")
    public ResponseEntity<?> createCategory(@RequestBody @Valid UpdateCategoryRequest request) {

        categoryService.createCategory(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
