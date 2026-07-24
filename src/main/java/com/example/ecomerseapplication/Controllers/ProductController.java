package com.example.ecomerseapplication.Controllers;

import com.example.ecomerseapplication.Auth.helpers.UserIdExtractor;
import com.example.ecomerseapplication.DTOs.requests.*;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PostOrUpdateReviewForbiddenException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.ReviewSoftDeletedException;
import com.example.ecomerseapplication.Mappers.ReviewMapper;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorMessage;
import com.example.ecomerseapplication.CustomErrorHelpers.ErrorType;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Utils.ImageValidator;
import com.example.ecomerseapplication.Utils.SortHelper;
import com.example.ecomerseapplication.enums.ProductSortType;
import com.example.ecomerseapplication.enums.ReviewSortType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Validated
@RestController
@RequestMapping("product/")
public class ProductController {

    private final ProductService productService;
    private final CategoryAttributeService categoryAttributeService;
    private final CustomerService customerService;
    private final ReviewService reviewService;
    private final CategoryService categoryService;
    private final ManufacturerService manufacturerService;
    private final PurchaseCartService purchaseCartService;
    private final UserIdExtractor userIdExtractor;
    private final SessionService sessionService;
    private final ProductRowService productRowService;
    private final ImageSearchService imageSearchService;

    @Autowired
    public ProductController(ProductService productService, CategoryAttributeService categoryAttributeService, CustomerService customerService, ReviewService reviewService, CategoryService categoryService, ManufacturerService manufacturerService, PurchaseCartService purchaseCartService, UserIdExtractor userIdExtractor, SessionService sessionService, ProductRowService productRowService, ImageSearchService imageSearchService) {
        this.productService = productService;
        this.categoryAttributeService = categoryAttributeService;
        this.customerService = customerService;
        this.reviewService = reviewService;
        this.categoryService = categoryService;
        this.manufacturerService = manufacturerService;
        this.purchaseCartService = purchaseCartService;
        this.userIdExtractor = userIdExtractor;
        this.sessionService = sessionService;
        this.productRowService = productRowService;
        this.imageSearchService = imageSearchService;
    }

    @GetMapping("findall")
    public ResponseEntity<PageResponse<CompactProductResponse>> findAll(@RequestParam @NotNull int page) {

        Sort sort = SortHelper.buildProdSort(ProductSortType.POPULARITY.getValue()).and(SortHelper.buildProdSort(ProductSortType.PRODUCT_ID.getValue()));
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit, sort);

        return ResponseEntity.ok(PageResponse.
                from(productService.findAllByRatingResponsePage(pageRequest))
        );
    }

    @GetMapping("search")
    public ResponseEntity<PageResponse<CompactProductResponse>> findByNameLike(@RequestParam @NotBlank String name,
                                                                               @NotNull @RequestParam int page,
                                                                               @RequestParam(required = false, name = "sort") String sortOrder
    ) {

        if (sortOrder == null || sortOrder.isBlank() || !ProductSortType.isValid(sortOrder)) {
            sortOrder = ProductSortType.RELEVANCE.getValue();
        }

        PageRequest pageRequest;
        Page<CompactProductResponse> responsePages;

//        System.out.println("name: " + name + " sort: " + sortOrder + " page: " + page);

        if (sortOrder == null || sortOrder.equals(ProductSortType.RELEVANCE.getValue())) {
            pageRequest = PageRequest.of(page, PageContentLimit.limit);
            responsePages = productService.getProductsByRelevance(pageRequest, name);
        } else {
            pageRequest = PageRequest.of(page, PageContentLimit.limit);
            responsePages = productService.getProductsLikeNameSort(pageRequest, name, sortOrder);
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(responsePages));
    }

    @GetMapping("suggest")
    public ResponseEntity<List<String>> getNameSuggestions(@RequestParam @NotBlank String name) {
        return ResponseEntity.ok(productService.getNameSuggestions(name));
    }

    @GetMapping("detail/{productCode}")
    public ResponseEntity<DetailedProductResponse> detailedProductInfo(@PathVariable String productCode) {

//        System.out.println("In detailed product endpoint: " + productCode + " ");



        DetailedProductResponse response = productService.getByCodeAndWithSession(productCode);

//        System.out.println("Product detail response: " + response);

        return ResponseEntity.ok(response);

    }

    @GetMapping("{productCode}/review/overview")
    public ResponseEntity<?> getReviewsOverview(@PathVariable String productCode) {
        return ResponseEntity.ok(reviewService.getRatingOverview(productCode));
    }

    @GetMapping("manufacturer/{manufacturerName}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productsByManufacturer(@PathVariable String manufacturerName,
                                                                                       @PathVariable int page,
                                                                                       @RequestParam(required = false, name = "sort") String sortOrder) {

//        System.out.println("Chosen sort: " + ((sortOrder!=null&&!sortOrder.isBlank())? sortOrder: "none") );


        Manufacturer manufacturer = manufacturerService.findByName(manufacturerName);

        Page<CompactProductResponse> productResponsePage = productService.getByManufacturer(manufacturer, page, sortOrder);

//        System.out.println("Sorted content: "+ productResponsePage.getContent());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));
    }

    @GetMapping("category/{name}/p{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> getProductsByCategory(@PathVariable String name,
                                                                                      @PathVariable int page,
                                                                                      @RequestParam(required = false, name = "sort") String sortOrder) {

        if (name.equals("Бензинови машини") || name.equals("електрически машини"))
            return ResponseEntity.notFound().build();

        ProductCategory productCategory = categoryService.findByNameActive(name);

        Page<CompactProductResponse> productResponsePage = productService.getByCategory(productCategory,
                page,
                sortOrder,
                PageContentLimit.limit);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(PageResponse.from(productResponsePage));
    }

    @PostMapping("filter/{page}")
    public ResponseEntity<PageResponse<CompactProductResponse>> productByFilterAndManufacturer(@RequestBody @Valid ProductFilterRequest productFilterRequest,
                                                                                               @PathVariable int page) {

//        System.out.println("Filter request: " + productFilterRequest);

        Set<CategoryAttribute> categoryAttributeSet = new HashSet<>();

        if (productFilterRequest.filterAttributes != null) {
            categoryAttributeSet = categoryAttributeService.getByNamesAndOptions(productFilterRequest.filterAttributes);
        }

        List<Manufacturer> manufacturerList = new ArrayList<>();

        if (productFilterRequest.manufacturerNames != null)
            manufacturerList = manufacturerService.getByNames(productFilterRequest.manufacturerNames);

        ProductCategory productCategory = categoryService.findByNameActive(productFilterRequest.productCategory);


        return ResponseEntity.ok(PageResponse.from(
                productService.getByCategoryFiltersManufacturerAndPriceRange(
                        categoryAttributeSet,
                        productCategory,
                        productFilterRequest.priceLowest,
                        productFilterRequest.priceHighest,
                        manufacturerList,
                        productFilterRequest.rating,
                        productFilterRequest.sortOrder,
                        page)));
    }

    @PostMapping("reviews/paged")
    public ResponseEntity<PageResponse<ReviewResponse>> getPagedReviews(@RequestBody @Valid ReviewSortRequest request) {

//        System.out.println("Inside reviews Paged endpoint: " + request.productCode() + "");

        Session session = sessionService.getRequestSession();

        Sort sort = request.sortOrder().getValue().equalsIgnoreCase(ReviewSortType.NEWEST.getValue())
                ? Sort.by("postTimestamp").descending()
                : Sort.by("postTimestamp").ascending();

        sort = sort.and(Sort.by("id").descending());

        PageRequest pageRequest = PageRequest.of(request.page(), PageContentLimit.limit, sort);

        Page<ReviewResponse> reviewPage;

        reviewPage = reviewService.getProductReviews(request, pageRequest, session);

        var response = PageResponse.from(reviewPage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("review/specific")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> getSpecificReviewData(@RequestParam("productCode") @NotBlank String productCode) {

//        System.out.println("IUD " + userId + " PCODE " + productCode);
        String customerId = userIdExtractor.getUserId();

        Review review = reviewService.getByUIDAndPCode(productCode, customerId);

//        System.out.println("REVIEW: " + review.getId() );
        if (review != null) {
            if (review.getIsDeleted()) {
                throw new ReviewSoftDeletedException("Review is soft deleted");
            } else {
                if (isUpdateTimeOver(review))
                    throw new PostOrUpdateReviewForbiddenException("This user cannot post a new review for this product, or update the existing one");

                ReviewContentResponse response = ReviewMapper.entToContentResponse(review);
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.ok(new ReviewContentResponse("", null, false));
    }

    @PostMapping("review/add")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> addReview(@RequestBody @Valid ReviewCreateRequest request) {

        reviewService.requestValidation(request.rating(), request.reviewText());

        String userId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(userId);
        Product product = productService.findByPCode(request.productCode());

        if (reviewService.exists(product, customer)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Заявката е отказана",
                    HttpStatus.CONFLICT.value(),
                    "Вече имате ревю за този продукт"));
        }

        Boolean isVerifiedCustomer = purchaseCartService.isProductPurchased(product.getProductCode(), customer.getKeycloakId());

        Product updatedProduct = reviewService.createReview(product, customer, request, isVerifiedCustomer);

        productService.save(updatedProduct);

        return ResponseEntity.status(HttpStatus.CREATED).body("Ревюто е качено!");
    }

    @PatchMapping("review/update")
    @Transactional
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<?> updateReview(@RequestBody @Valid ReviewUpdateRequest request) {

        reviewService.requestValidation(request.rating, request.reviewText);

        String customerId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(customerId);
        Product product = productService.findByPCode(request.productCode);
        Review review = reviewService.getByProdAndCust(product, customer);

        if (review.getIsDeleted()) {
            return ResponseEntity.notFound().build();
        }

        if (isUpdateTimeOver(review))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Не може да се добавят повече ревюта",
                    HttpStatus.FORBIDDEN.value(),
                    "Вече сте добавили ревю за този продукт. Срокът за редакция е изтекъл и не могат да се правят промени."));

        Product updatedProduct = reviewService.updateReview(review, request, product);//todo po4i cqlata logika po update-a trqbva da e tuk

        if (updatedProduct == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ErrorType.DUPLICATION_OF_DATA,
                    "Не бе извършена промяна",
                    HttpStatus.BAD_REQUEST.value(),
                    ErrorMessage.DUPLICATION_OF_REVIEW_DATA));
        }

        try {
            productService.save(updatedProduct);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }

    private boolean isUpdateTimeOver(Review review) {
        Instant now = Instant.now();
        Instant deadline = review.getPostTimestamp().plus(24, ChronoUnit.HOURS);

        return now.isAfter(deadline);
    }

    @DeleteMapping("review/delete")
    @PreAuthorize("hasRole(@roles.customer())")
    public ResponseEntity<String> deleteReview(@RequestParam("product_code") @NotBlank String productCode) {

        String customerId = userIdExtractor.getUserId();
        Customer customer = customerService.getById(customerId);
        Product product = productService.findByPCode(productCode);
        Review review = reviewService.getByProdAndCust(product, customer);

        if (review.getIsDeleted())
            return ResponseEntity.notFound().build();

//        short newRating = reviewService.updatedRating(product, review);
//
//        if (newRating == -1)
//            return ResponseEntity.internalServerError().build();

//        product.setRating(newRating);
//        product.getReviews().remove(review);
//        productService.save(product); todo tova moje da se sloji za drug method, moje bi adminski, koito specialno iztriva review-ta ZADULJITELNO SLOJI UPDATE NA POLETO ZA BROI NA REVIEW-TATA KATO SE IZTRIE OKON4ATELNO REVIEW ZA PRODUKTA!!!
        try {
            reviewService.softDelete(review);
        } catch (Exception e) {
            System.out.println("Error soft deleting review: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().body("Ревюто е изтрито");
    }

    @GetMapping("topSalesProducts")
    public ResponseEntity<?> getTopSalesProducts() {
        return ResponseEntity.ok(productRowService.getTopActiveSaleProducts());
    }

    @GetMapping("topCategoryProducts")
    public ResponseEntity<?> getTopCategoryProducts() {
        return ResponseEntity.ok(productRowService.getTopCategoryProducts());
    }

    @PostMapping("codes/stockValidation")
    public ResponseEntity<?> getProductsByCodesWithStockValidation(@RequestBody List<ProductCodeQuantityPairRequest> pairRequestList) {

//        System.out.println("In product fetch for purchase: " + pairRequestList);

        List<CompactProductQuantityPairResponse> response = productService.findByCodesAndQuantityInspect(pairRequestList);

//        System.out.println("Response: " + response);

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "imageSearch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getProductsByImage(@RequestParam("image") MultipartFile image) throws IOException {
        BufferedImage bufferedImage =
                ImageValidator.validateImageInput(image);

        return ResponseEntity.ok(imageSearchService.findByImage(bufferedImage));
    }

    @PostMapping("catManSearch")
    public ResponseEntity<?> getByCategoryAndManufacturerLists(@RequestBody CatManRequest request) {

        List<ProductCategory> categories = categoryService.getAllByNames(request.categories());
        List<Manufacturer> manufacturers = manufacturerService.getByNames(request.manufacturers());

        return ResponseEntity.ok(productService.getByCategoriesAndManufacturers(categories,
                manufacturers,
                request.page()));
    }
}
