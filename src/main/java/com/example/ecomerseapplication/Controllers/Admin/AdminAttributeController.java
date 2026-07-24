package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.Services.Admin.AdminAttributeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/attribute/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminAttributeController {

    private final AdminAttributeService adminAttributeService;

    public AdminAttributeController(AdminAttributeService adminAttributeService) {
        this.adminAttributeService = adminAttributeService;
    }

    @GetMapping("attribute-group/all")
    public ResponseEntity<?> getAllAttributesGroups() {
        return ResponseEntity.ok(adminAttributeService.getAllDetailedAttributeGroups());
    }

    @GetMapping("get-for-product/{id}")
    public ResponseEntity<?> getAttributesForProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(adminAttributeService.getAttributesOfProductAndCategory(id));
    }
}
