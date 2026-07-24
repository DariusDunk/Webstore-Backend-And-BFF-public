package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.requests.ManufacturerFormRequest;
import com.example.ecomerseapplication.Services.Admin.AdminManufacturerService;
import com.example.ecomerseapplication.Services.ManufacturerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/manufacturer/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminManufacturerController {

    private final ManufacturerService manufacturerService;
    private final AdminManufacturerService adminManufacturerService;

    public AdminManufacturerController(ManufacturerService manufacturerService, AdminManufacturerService adminManufacturerService) {
        this.manufacturerService = manufacturerService;
        this.adminManufacturerService = adminManufacturerService;
    }

    @GetMapping("all")
    public ResponseEntity<?> getAllManufacturers() {
        return ResponseEntity.ok(manufacturerService.getAllCompact());
    }

    @GetMapping("get/{id}")
    public ResponseEntity<?> getManufacturerById(@PathVariable Integer id) {
        return ResponseEntity.ok(manufacturerService.getByIdCompact(id));
    }

    @PostMapping("create")
    public ResponseEntity<?> createManufacturer(@RequestBody ManufacturerFormRequest request) {

        adminManufacturerService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("update/{id}")
    public ResponseEntity<?> updateManufacturer(@PathVariable Integer id, @RequestBody ManufacturerFormRequest request) {
        adminManufacturerService.updateManufacturer(id, request);

        return ResponseEntity.noContent().build();
    }
}
