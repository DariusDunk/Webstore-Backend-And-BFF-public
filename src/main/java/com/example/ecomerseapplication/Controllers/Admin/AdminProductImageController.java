package com.example.ecomerseapplication.Controllers.Admin;

import com.example.ecomerseapplication.DTOs.serverDtos.ProductAndImageContextForMinIOCleanupDto;
import com.example.ecomerseapplication.Services.Admin.AdminProductImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/product-image/")
@PreAuthorize("hasRole(@roles.admin())")
public class AdminProductImageController {

    private final AdminProductImageService adminProductImageService;

    public AdminProductImageController(AdminProductImageService adminProductImageService) {
        this.adminProductImageService = adminProductImageService;
    }

    @GetMapping("of-product/{id}")
    public ResponseEntity<?> getProductImages(@PathVariable Integer id) {
        return ResponseEntity.ok(adminProductImageService.getProductImages(id));
    }

    @PostMapping(path = "upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImagesForProduct(        @PathVariable Integer id,

                                                            @RequestPart(required = false) MultipartFile mainImageFile,

                                                            @RequestPart(required = false) List<MultipartFile> galleryFiles,

                                                            @RequestParam(required = false) Set<String> existingGalleryImages,

                                                            @RequestParam(required = false) Boolean replaceMainImage) throws Exception {

//        System.out.println("replaceMainImage: " + replaceMainImage + "");

        ProductAndImageContextForMinIOCleanupDto minIOImagesToDelete = adminProductImageService.updateProductImages(id,
                mainImageFile,
                galleryFiles,
                replaceMainImage,
                existingGalleryImages);

        try
        {
            adminProductImageService.deleteProductImages(minIOImagesToDelete);
        }
        catch (Exception e)
        {
            System.out.println("Error deleting images from MinIO: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
