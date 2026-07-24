package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Entities.CategoryAttribute;
import com.example.ecomerseapplication.Services.CategoryAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("attributes/")
public class CategoryAttributeController {


    private final CategoryAttributeService categoryAttributeService;

    @Autowired
    public CategoryAttributeController(CategoryAttributeService categoryAttributeService) {
        this.categoryAttributeService = categoryAttributeService;
    }

    @GetMapping("getattributes")
    public List<CategoryAttribute> findAll() {
        return categoryAttributeService.getAll();
    }

}
