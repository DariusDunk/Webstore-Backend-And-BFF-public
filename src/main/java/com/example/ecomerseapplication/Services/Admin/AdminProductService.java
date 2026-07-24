package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.ProductAttributeUpdateRequest;
import com.example.ecomerseapplication.DTOs.requests.ProductFormRequest;
import com.example.ecomerseapplication.DTOs.responses.AdminProductResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.responses.SaleProductSuggestionResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.CompactProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.DetailedProductProjection;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.DuplicatedAttributeException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.EmptyAttributeValueException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PessimisticLockOrTimeoutPurchaseException;
import com.example.ecomerseapplication.Mappers.ProductDTOMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.ProductRepository;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Utils.SortHelper;
import com.example.ecomerseapplication.enums.ProductSortType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminProductService {
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ManufacturerService manufacturerService;
    private final CategoryService categoryService;
    private final AttributeNameService attributeNameService;
    private final CategoryAttributeService categoryAttributeService;

    public AdminProductService(ProductService productService, ProductRepository productRepository, ManufacturerService manufacturerService, CategoryService categoryService, AttributeNameService attributeNameService, CategoryAttributeService categoryAttributeService) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
        this.attributeNameService = attributeNameService;
        this.categoryAttributeService = categoryAttributeService;
    }


    @Transactional
    public void updateProduct(ProductFormRequest request, int id) {
        Product product = productService.getById(id);
        ProductCategory category = categoryService.getById(request.categoryId());
        Manufacturer manufacturer = manufacturerService.getById(request.manufacturerId());

        product.updateProduct(request.productName(),
                request.originalPriceStotinki(),
                request.description(),
                category,
                request.productCode(),
                request.stockQuantity(),
                manufacturer,
                request.model());

    }

    @Transactional
    public int createProduct(ProductFormRequest request) {
        ProductCategory category = categoryService.getById(request.categoryId());
        Manufacturer manufacturer = manufacturerService.getById(request.manufacturerId());
        Product product = new Product();
        product.updateProduct(request.productName(),
                request.originalPriceStotinki(),
                request.description(),
                category,
                request.productCode(),
                request.stockQuantity(),
                manufacturer,
                request.model());

        product.setProductCategory(category);
        product.setManufacturer(manufacturer);

       return productRepository.save(product).getId();
    }

    public AdminProductResponse getByIdForAdminResponse(int id) {

        DetailedProductProjection projection = productRepository
                .getByIdDetProjection(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return ProductDTOMapper.detailedProjectionToAdminResponse(projection);
    }

    public PageResponse<AdminProductResponse> getAllProductsPaged(String page) {

        Sort sort = SortHelper.buildProdSort(ProductSortType.PRODUCT_ID.getValue());

        Page<DetailedProductProjection> productProjections = productRepository.getAllDetailedProductsPaged(
                PageRequest.of(Integer.parseInt(page), PageContentLimit.limit, sort)
        );

        return ProductDTOMapper.adminProductProjPageToResponsePage(productProjections);
    }

    public List<SaleProductSuggestionResponse> getSuggestionsForSale(String keyword) {
        List<CompactProductProjection> projections = productRepository.getNameSuggestionsForSaleForm(keyword);

        List<SaleProductSuggestionResponse> responseList = new ArrayList<>();

        for (CompactProductProjection projection : projections) {
            responseList.add(new SaleProductSuggestionResponse(projection.getName(), projection.getProductCode()));
        }

        return responseList;
    }

    @Transactional
    public void updateProductAttributes(Integer id, List<ProductAttributeUpdateRequest> request) {

        Product product;
        try {
            product = productService.getByIdWithAttributesAndLock(id);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new PessimisticLockOrTimeoutPurchaseException("Pessimistic lock encountered for fetching product in attributes update request",
                    "Заключен продукт",
                    "Избраният продукт е временно заключен за обработка, опитайте отново");
        }


        if (!request.isEmpty())
        {
            Map<Integer, String> requestMap;
            try {
                requestMap = request
                        .stream()
                        .collect(Collectors
                                .toMap(ProductAttributeUpdateRequest::nameId,
                                        r -> normalize(r.value()))
                        );
            } catch (IllegalStateException ex) {
                System.out.println(ex.getMessage());
                throw new DuplicatedAttributeException("Duplicated attribute names in request!");

            }

            List<AttributeName> attributeNames = attributeNameService.getByIdsWithOptions(requestMap
                    .keySet()
                    .stream()
                    .toList());

            Map<Integer, Map<String, CategoryAttribute>> attributeNameOptionMap = attributeNames
                    .stream()
                    .collect
                            (Collectors.toMap(AttributeName::getId,
                                    an -> an.getCategoryAttributeList()
                                            .stream()
                                            .collect(Collectors
                                                    .toMap(ca -> normalize(ca.getAttributeOption()),
                                                            Function.identity())))
                            );
            List<CategoryAttribute> processedAttributes = processProductAttributes(
                    attributeNameOptionMap,
                    requestMap,
                    attributeNames);

            processedAttributes = categoryAttributeService.saveAll(processedAttributes);

            product.getCategoryAttributeSet().clear();
            product.getCategoryAttributeSet().addAll(processedAttributes);
        }
        else
        {
            product.getCategoryAttributeSet().clear();
        }

    }

    private List<CategoryAttribute> processProductAttributes(Map<Integer, Map<String, CategoryAttribute>> attributeMap,
                                                             Map<Integer, String> requestMap,
                                                             List<AttributeName> attributeNames) {

        List<CategoryAttribute> newAttributes = new ArrayList<>();

        for (AttributeName attributeName: attributeNames) {

            String newAttributeValue = normalize(requestMap.get(attributeName.getId()));

            if (newAttributeValue == null||newAttributeValue.isBlank()) {
                throw new EmptyAttributeValueException("Attribute value cannot be empty");
            }

            Map<String, CategoryAttribute> inner =
                    attributeMap.get(attributeName.getId());

            CategoryAttribute oldAttribute =
                    inner != null ? inner.get(newAttributeValue) : null;

            if (oldAttribute == null) {
                CategoryAttribute newAttribute = new CategoryAttribute();
                newAttribute.setAttributeName(attributeName);

                newAttribute.setAttributeOption(newAttributeValue);

                newAttributes.add(newAttribute);
            }
            else {
                newAttributes.add(oldAttribute);
            }
        }
        return newAttributes;

    }

    private String normalize(String v) {
        return v == null ? null : v.trim().toLowerCase();
    }

    public void refreshStockOfPurchaseProducts(List<PurchaseCart> purchaseCarts) {

        Set<Integer> productIds = purchaseCarts
                .stream()
                .map(pc-> pc.getPurchaseCartId().getProduct().getId())
                .collect(Collectors.toSet());

        List<Product> purchaseProducts;

        try
        {
            purchaseProducts = productService.getByIdSetWithLock(productIds);
        }
        catch (PessimisticLockException | LockTimeoutException e)
        {
            throw new PessimisticLockOrTimeoutPurchaseException("One or more of the requested produts for stock increase are locked for edits",
                    "Заключени продукту",
                    "Един или повече от продуктите за тази поръчка в момента са заключени за промяна, опитайте отново");
        }

        Map<Integer, Product> productMap = purchaseProducts
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (PurchaseCart purchaseCart : purchaseCarts) {
            Product product = productMap.get(purchaseCart.getPurchaseCartId().getProduct().getId());

            if (product != null) {
                int newStock = purchaseCart.getQuantity() + product.getQuantityInStock();
                product.setQuantityInStock(newStock);
            }
        }
    }


}
