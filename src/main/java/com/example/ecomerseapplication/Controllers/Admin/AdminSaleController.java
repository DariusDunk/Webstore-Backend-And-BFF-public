package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.SaleCreateRequest;
import com.example.ecomerseapplication.DTOs.requests.SaleUpdateRequest;
import com.example.ecomerseapplication.Services.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/sale/")
@PreAuthorize("hasRole(@roles.admin())")
@Validated
public class AdminSaleController {

    private final SaleService saleService;

    public AdminSaleController(SaleService saleService) {
        this.saleService = saleService;
    }


    @GetMapping("all/{page}")
    public ResponseEntity<?> getAllSales(@PathVariable int page) {
        return ResponseEntity.ok(saleService.getAllSales(page));
    }

    @GetMapping("detailed/{id}")
    public ResponseEntity<?> getDetailedSale(@PathVariable long id) {
        return ResponseEntity.ok(saleService.getDetailedSaleById(id));
    }

    @PatchMapping("update")
    public ResponseEntity<?> updateSale(@RequestBody @Valid SaleUpdateRequest request) {

        saleService.updateSale(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("create")
    public ResponseEntity<?> createSale(@RequestBody @Valid SaleCreateRequest request) {

        saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
