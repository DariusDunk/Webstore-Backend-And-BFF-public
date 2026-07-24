package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Services.ManufacturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("manufacturer/")
public class ManufacturerController {


    private final ManufacturerService manufacturerService;

    @Autowired
    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

//    @GetMapping("manufacturers")
//    public List<Manufacturer> getALl() {
//        return manufacturerService.getAll();
//    }
}
