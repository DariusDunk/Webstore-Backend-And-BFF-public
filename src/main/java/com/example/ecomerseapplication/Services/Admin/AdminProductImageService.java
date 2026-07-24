package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.ProductImageResponse;
import com.example.ecomerseapplication.DTOs.responses.ProductImagesSectionResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.ProductAndImageContextForMinIOCleanupDto;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.ProductImage;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PessimisticLockOrTimeoutPurchaseException;
import com.example.ecomerseapplication.Services.MinioService;
import com.example.ecomerseapplication.Services.ProductService;
import com.example.ecomerseapplication.Utils.FileNameSanitizer;
import com.example.ecomerseapplication.Utils.ImageValidator;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminProductImageService {

    private final ProductService productService;
    private final MinioService minioService;

    public AdminProductImageService(ProductService productService, MinioService minioService) {
        this.productService = productService;
        this.minioService = minioService;
    }

    public ProductImagesSectionResponse getProductImages(Integer productId) {
        Product product = productService.getByIdWithImages(productId);

        ProductImageResponse mainImageResponse = new ProductImageResponse(product.getMainImageUrl(),
                product.getProductCode());
        List<ProductImageResponse> productImagesResponse = product
                .getProductImages()
                .stream()
                .map(pr -> new ProductImageResponse(pr.getImageFileName(), product.getProductCode()))
                .toList();

        return new ProductImagesSectionResponse(mainImageResponse, productImagesResponse);
    }

    public void deleteProductImages(ProductAndImageContextForMinIOCleanupDto dto) throws Exception {

        String productCode = dto.productCode();
        Set<String> imageNames = dto.imageNames();

        String subDir = productCode + "/";

        for (String imageName : imageNames) {
            String objectName = subDir + imageName;
            minioService.deleteFile(objectName);
        }
    }

    @Transactional
    public ProductAndImageContextForMinIOCleanupDto updateProductImages(Integer productId,
                                                                        MultipartFile mainImage,
                                                                        List<MultipartFile> newGalleryImages,
                                                                        Boolean replaceMainImage,
                                                                        Set<String> existingGalleryImagesNames) throws Exception {


        Product product;
        try {
            product = productService.getByIdWithImagesAndLocking(productId);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new PessimisticLockOrTimeoutPurchaseException("Failed to fetch locked product for image update id: " + productId,
                    "Заключен продукт",
                    "Избраният продукт е заключен за обработка в този момент, опитайте отново");
        }
        String subDir = product.getProductCode() + "/";

        String dbMainImageName = null;
        NewGalleryImageData newMainImageData;

        Set<String> finalExistingNames;
        List<NewGalleryImageData> finalNewGalleryImages;
        Set<String> initialDbImageNames = product
                .getProductImages()
                .stream()
                .map(ProductImage::getImageFileName)
                .collect(Collectors.toSet());

        GalleryImagesData galleryImagesData = sanitizeGalleryImages(existingGalleryImagesNames,
                newGalleryImages
        );

        finalExistingNames = galleryImagesData.cleanedExistingNames();
        finalNewGalleryImages = galleryImagesData.cleanedNewGalleryImages();

        if (replaceMainImage) {
            dbMainImageName = product.getMainImageUrl();

            if (dbMainImageName != null)
                initialDbImageNames.add(dbMainImageName);

//            System.out.println("dbMainImageName: " + dbMainImageName + "");

            newMainImageData = mainImageHandler(mainImage, product);
            if (newMainImageData != null)
                finalNewGalleryImages.add(newMainImageData);
        }

        finalNewGalleryImages = handleDuplicates(finalExistingNames, finalNewGalleryImages);
        Set<String> finalNewImagesNames = finalNewGalleryImages
                .stream()
                .map(NewGalleryImageData::fileName)
                .collect(Collectors.toSet());

        Set<String> finalNameList = new HashSet<>(finalNewImagesNames);
        finalNameList.addAll(finalExistingNames);

        Set<String> dbImagesToDelete = product.getProductImages()
                .stream()
                .map(ProductImage::getImageFileName)
                .collect(Collectors.toSet());

        if (replaceMainImage) {
            dbImagesToDelete.add(dbMainImageName);

            Optional<NewGalleryImageData> rediscoveredMainImage =
                    finalNewGalleryImages.stream()
                            .filter(img -> img.imageRole == ImageRole.MAIN)
                            .findFirst();

            if (rediscoveredMainImage.isPresent()) {

//                System.out.println("sanitized main image name: " + rediscoveredMainImage.get().fileName() + "");

                product.setMainImageUrl(
                        rediscoveredMainImage.get().fileName());
            }
            else {
//                System.out.println("no main image found, marking product as not having a main image");
                product.setMainImageUrl(null);
            }
        }

        dbImagesToDelete.removeAll(finalNameList);
//        System.out.println("images to delete: " + dbImagesToDelete + "");
        Set<String> uploaded = minIoUpload(finalNewGalleryImages, subDir, initialDbImageNames);
        uploaded.removeAll(initialDbImageNames);
        try {
            performDBops(dbImagesToDelete, product, finalNewGalleryImages);
        }
        catch (Exception e) {
            System.out.println("Error performing DB operations: " + e.getMessage());
            rollbackMinioUploads(uploaded);
            throw e;
        }
        return new ProductAndImageContextForMinIOCleanupDto(product.getProductCode(), dbImagesToDelete);

    }

    private void performDBops(Set<String> dbImagesToDelete,
                              Product product,
                              @MonotonicNonNull List<NewGalleryImageData> finalNewGalleryImages) {

        List<String> finalNewNameList = finalNewGalleryImages
                .stream()
                .filter(image->image.imageRole.equals(ImageRole.GALLERY))
                .map(NewGalleryImageData::fileName)
                .toList();

        Set<String> existingDBImageNames = product.getProductImages()
                .stream()
                .map(ProductImage::getImageFileName)
                .collect(Collectors.toSet());

        List<ProductImage> newImageEntities = finalNewNameList
                .stream()
                .filter(name-> !existingDBImageNames.contains(name))
                .map(name->new ProductImage(name, product))
                .toList();

        product.getProductImages().removeIf(img ->
                dbImagesToDelete.contains(img.getImageFileName())
        );
        product.getProductImages().addAll(newImageEntities);

    }

    private enum ImageRole {
        MAIN,
        GALLERY
    }


    private Set<String> minIoUpload(List<NewGalleryImageData> finalNewGalleryImages,
                             String subDir,
                                    Set<String> initialDbImageNames) throws Exception {

        Set<String> uploaded = new HashSet<>();

        try
        {
            for (NewGalleryImageData newImageData : finalNewGalleryImages) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                ImageIO.write(newImageData.image, newImageData.format, baos);

                String objectName = subDir + newImageData.fileName;

                minioService.uploadFile(
                        objectName,
                        new ByteArrayInputStream(baos.toByteArray()),
                        newImageData.contentType
                );

                uploaded.add(objectName);
            }
        }
        catch (Exception e)
        {
            uploaded.removeAll(initialDbImageNames);
            rollbackMinioUploads(uploaded);

            throw e;
        }

        return uploaded;
    }

    private void rollbackMinioUploads(Set<String> uploaded) {

        for (String objectName : uploaded) {
            try {
                minioService.deleteFile(objectName);
            } catch (Exception ignore) {

                System.out.println("Failed to rollback MinIO object: " + objectName);
            }
        }
    }

    private List<NewGalleryImageData> handleDuplicates(Set<String> finalExistingNames,
                                                       List<NewGalleryImageData> finalNewGalleryImages) {

        Set<String> occupiedNames = new HashSet<>(finalExistingNames);

//        System.out.println("existing names before duplicate resolving : " + occupiedNames + " \nfinalNewGalleryImages: " + finalNewGalleryImages);

        return finalNewGalleryImages
                .stream()
                .map(img -> resolveDuplicate(img, occupiedNames))
                .toList();
    }

    private NewGalleryImageData resolveDuplicate(NewGalleryImageData newImageData,
                                                 Set<String> occupiedNames) {

        if (occupiedNames.contains(newImageData.fileName())) {
            String newName;
            int i = 1;
            do {

                newName = generateDuplicateName(newImageData.fileName(), i);
                i++;
            }
            while (occupiedNames.contains(newName));

            occupiedNames.add(newName);
            return new NewGalleryImageData(newName,
                    newImageData.image(),
                    newImageData.imageRole,
                    newImageData.format,
                    newImageData.contentType);
        }
        return newImageData;

    }

    private String generateDuplicateName(String fileName, int index) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex <= 0) {
            return fileName + "(" + index + ")";
        }

        String baseName = fileName.substring(0, dotIndex);
        String extension = fileName.substring(dotIndex);

        return baseName + "(" + index + ")" + extension;
    }

    private String getExtension(String filename) {
        if (filename == null) return "";

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(dotIndex + 1).toLowerCase();
    }

    private GalleryImagesData sanitizeGalleryImages(Set<String> existingGalleryImagesNames,
                                                    List<MultipartFile> newGalleryImages) throws IOException {


        Set<String> cleanedExisting = new HashSet<>();

        if (existingGalleryImagesNames != null) {
            cleanedExisting = existingGalleryImagesNames.stream()
                    .map(FileNameSanitizer::sanitizeFileName)
                    .collect(Collectors.toSet());
        }

        List<NewGalleryImageData> finalNewGalleryImages = new ArrayList<>();

        if (newGalleryImages != null) {

            for (MultipartFile newGalleryImage : newGalleryImages) {
                String sanitizedFileName = FileNameSanitizer.sanitizeFileName(newGalleryImage.getOriginalFilename());
                BufferedImage image = ImageValidator.validateImageInput(newGalleryImage);

//                String fileFormat = getExtension(sanitizedFileName);
//                String contentType = newGalleryImage.getContentType();
//
//                if (contentType == null || contentType.equals("application/octet-stream")) {
//                    contentType = switch (fileFormat) {
//                        case "png" -> "image/png";
//                        case "jpg", "jpeg" -> "image/jpeg";
//                        default -> "application/octet-stream";
//                    };
//                }
                AdditionalImageMetadata metadata = getAdditionalImageMetadata(newGalleryImage, sanitizedFileName);

                finalNewGalleryImages.add(new NewGalleryImageData(sanitizedFileName,
                        image,
                        ImageRole.GALLERY,
                        metadata.fileFormat,
                        metadata.contentType));
            }
        }

        return new GalleryImagesData(cleanedExisting, finalNewGalleryImages);
    }

    private record NewGalleryImageData(
            String fileName,
            BufferedImage image,
            ImageRole imageRole,
            String format,
            String contentType
    ) {
    }

    private record GalleryImagesData(Set<String> cleanedExistingNames,
                                     List<NewGalleryImageData> cleanedNewGalleryImages) {
    }


    private NewGalleryImageData mainImageHandler(MultipartFile mainImage, Product product) throws IOException {

        if (mainImage == null || mainImage.isEmpty()) {
            product.setMainImageUrl(null);
            return null;
        } else {
            BufferedImage finalMainImage = ImageValidator.validateImageInput(mainImage);
            String sanitizedMianName = FileNameSanitizer.sanitizeFileName(mainImage.getOriginalFilename());
            AdditionalImageMetadata metadata = getAdditionalImageMetadata(mainImage, sanitizedMianName);
            return new NewGalleryImageData(sanitizedMianName,
                    finalMainImage,
                    ImageRole.MAIN,
                    metadata.fileFormat(),
                    metadata.contentType()
                    );
        }

    }

    @NotNull
    private AdditionalImageMetadata getAdditionalImageMetadata(MultipartFile mainImage, String sanitizedMianName) {
        String fileFormat = getExtension(sanitizedMianName);
        String contentType = mainImage.getContentType();

        if (contentType == null || contentType.equals("application/octet-stream")) {
            contentType = switch (fileFormat) {
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                default -> "application/octet-stream";
            };
        }
        return new AdditionalImageMetadata(fileFormat, contentType);
    }

    private record AdditionalImageMetadata(String fileFormat, String contentType) {
    }

}

