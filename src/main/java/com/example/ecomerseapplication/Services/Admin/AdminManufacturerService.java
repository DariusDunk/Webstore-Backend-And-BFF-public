package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.ManufacturerFormRequest;
import com.example.ecomerseapplication.Entities.Manufacturer;
import com.example.ecomerseapplication.Repositories.ManufacturerRepository;
import com.example.ecomerseapplication.Services.ManufacturerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminManufacturerService {

    private final ManufacturerService manufacturerService;
    private final ManufacturerRepository manufacturerRepository;

    public AdminManufacturerService(ManufacturerService manufacturerService, ManufacturerRepository manufacturerRepository) {
        this.manufacturerService = manufacturerService;
        this.manufacturerRepository = manufacturerRepository;
    }

    public void create(ManufacturerFormRequest request) {

        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setManufacturerName(request.name());

        manufacturerRepository.save(manufacturer);
    }

    @Transactional
    public void updateManufacturer(Integer id, ManufacturerFormRequest request) {

        Manufacturer manufacturer = manufacturerService.getById(id);
        manufacturer.setManufacturerName(request.name());
    }
}
