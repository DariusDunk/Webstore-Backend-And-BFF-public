package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.ProductAttributeUpdateRequest;
import com.example.ecomerseapplication.DTOs.requests.ProductFormRequest;
import com.example.ecomerseapplication.Services.Admin.AdminProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/product/")
@PreAuthorize("hasRole(@roles.admin())")
@Validated
public class AdminProductController {

    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("suggestions")
    public ResponseEntity<?> getAllProductsSuggestions(@RequestParam() String keyword) {
        return ResponseEntity.ok(adminProductService.getSuggestionsForSale(keyword));
    }

    @GetMapping("all/p/{page}")
    public ResponseEntity<?> getAllProducts(@PathVariable String page) {
        return ResponseEntity.ok(adminProductService.getAllProductsPaged(page));
    }

    @GetMapping("detail/{id}")
    public ResponseEntity<?> getDetailedProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(adminProductService.getByIdForAdminResponse(id));
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id, @RequestBody ProductFormRequest request) {

        adminProductService.updateProduct(request, id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("create")
    public ResponseEntity<?> createProduct(@RequestBody ProductFormRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(adminProductService.createProduct(request));
    }

    @PatchMapping("update/{id}/attributes")
    public ResponseEntity<?> updateProductAttributes(@PathVariable Integer id, @RequestBody List<ProductAttributeUpdateRequest> request) {
        adminProductService.updateProductAttributes(id, request);
        return ResponseEntity.noContent().build();
    }

}
