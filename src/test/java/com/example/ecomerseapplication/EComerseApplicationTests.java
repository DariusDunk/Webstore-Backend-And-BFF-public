package com.example.ecomerseapplication;
import com.example.ecomerseapplication.DTOs.responses.*;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOptionDTO;
import com.example.ecomerseapplication.Entities.*;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.*;
import com.example.ecomerseapplication.Services.*;
import com.example.ecomerseapplication.Services.Admin.AdminAttributeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

@SpringBootTest
@ActiveProfiles("test")
class EComerseApplicationTests {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FavoriteOfCustomerService favoriteOfCustomerService;
    @Autowired
    private AdminAttributeService adminAttributeService;

    @Test
    void contextLoads() {
    }

    @Autowired
    CategoryAttributeService categoryAttributeService;

    @Autowired
    CartProductService cartProductService;

    @Autowired
    CustomerService customerService;


    @Test
    void getAttributeByNameAndOption(){

        Map<String, List<String> > attMap = new HashMap<>();
        attMap.put("Широчина на фрезоване", List.of( "1000-1200", "1200-1400"));

//        Set<CategoryAttribute> sets = categoryAttributeRepository.findByNamesAndOptions(names, options);
        Set<CategoryAttribute> sets = categoryAttributeService.getByNamesAndOptions(attMap);

        System.out.println(sets.size());
        System.out.println(sets);
    }

    @Test
    void getAttributesOfCategory(){
        List<AttributeOptionDTO> attributes = categoryRepository.getAttributesOfCategory(5);

        for ( AttributeOptionDTO attributeOptionDTO : attributes) {
            System.out.println(attributeOptionDTO);
        }
    }


    @Test
    void getReviewOverviewByProductCode() {
        System.out.println(reviewRepository.getRatingOverviewByProductCode("20621303"));
    }

    @Test
    void checkTimeZones() {
        System.out.println("Timezone: "+TimeZone.getDefault());
    }

    @Test
    void getFavouritesTest() {
        Customer customer = customerService.getById("a5668417-ddc8-4029-9fcb-4f61512d044f");
        PageResponse<CompactProductResponse> fetchResponse = favoriteOfCustomerService.getFromCustomerPaged(customer, PageRequest.of(0, PageContentLimit.limit));

        System.out.println("Fetch result: " +fetchResponse.content());
    }




    @Test
    void testCartSummary() {
        Customer customer = customerService.getById("a5668417-ddc8-4029-9fcb-4f61512d044f");

       CartSummaryResponse cartSummaryResponse = cartProductService.getSummary(customer);

       System.out.println("Cart summary response: "+cartSummaryResponse);
    }

    @Test
    void testAttributesOfProductQuery() {
        int productId = 3;

        AttributesOfProductAndCategory attributesOfProductAndCategory = adminAttributeService.getAttributesOfProductAndCategory(productId);

        System.out.println("Product attributes:\n");
        System.out.println("Product name: " + attributesOfProductAndCategory.productName());
        for (AttributeOfProductResponse attribute : attributesOfProductAndCategory.productAttributes()) {
            System.out.println(" \n----------------------------------\n" +
                    "attribute name: " + attribute.attributeName()
                    + "\n attribute name id: " + attribute.attributeNameId()
            + "\nmeasurement unit: " + attribute.measurementUnit()
            + "\nvalue: " + attribute.attributeValue()
            + "\nis in category: "+ attribute.isInCategory()
            + " \n" +
                    "----------------------------------\n");
        }

        System.out.println("Category attributes:");
        for (CompactAttributeResponse attribute : attributesOfProductAndCategory.categoryAttributes()) {
            System.out.println(" \n----------------------------------\n" +
                    "attribute name: " + attribute.attributeName()
                    + "\n attribute name id: " + attribute.attributeNameId()
                    + " \n" +
                    "----------------------------------\n");
        }
    }


}
