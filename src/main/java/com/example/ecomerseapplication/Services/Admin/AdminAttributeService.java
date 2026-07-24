package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.AttributeOfProductResponse;
import com.example.ecomerseapplication.DTOs.responses.AttributesOfProductAndCategory;
import com.example.ecomerseapplication.DTOs.responses.CompactAttributeResponse;
import com.example.ecomerseapplication.DTOs.responses.DetailedAttributeGroupResponse;
import com.example.ecomerseapplication.DTOs.serverDtos.AttributeOfGroupDTO;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Mappers.AttributeMapper;
import com.example.ecomerseapplication.Services.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAttributeService {

    private final AttributeGroupService attributeGroupService;
    private final CategoryAttributeService categoryAttributeService;
    private final ProductService productService;

    public AdminAttributeService(AttributeGroupService attributeGroupService,
                                 CategoryAttributeService categoryAttributeService,
                                 ProductService productService) {
        this.attributeGroupService = attributeGroupService;
        this.categoryAttributeService = categoryAttributeService;
        this.productService = productService;
    }

    public List<DetailedAttributeGroupResponse> getAllDetailedAttributeGroups() {

        List<AttributeGroup> attributeGroups = attributeGroupService.getAll();

        List<AttributeOfProjection> attributeOfProjections = attributeGroupService.getAllGroupsWithAttributes();

        Map<Long, List<AttributeOfProjection>> groupAttributesMap = new HashMap<>();

        for (AttributeOfProjection attribute : attributeOfProjections) {
            if (groupAttributesMap.isEmpty()
                    || !groupAttributesMap.containsKey(attribute.getGroupId())) {

                groupAttributesMap.put(attribute.getGroupId(),
                        new ArrayList<>(List.of(attribute)));

            } else {
                groupAttributesMap.get(attribute.getGroupId()).add(attribute);
            }
        }
        List<DetailedAttributeGroupResponse> groupResponseList = new ArrayList<>();


        for (AttributeGroup attributeGroup : attributeGroups) {
            List<AttributeOfProjection> attributeProjections = groupAttributesMap
                    .get(attributeGroup.getId());

            List<AttributeOfGroupDTO> attributesDto = new ArrayList<>();
            if (attributeProjections != null && !attributeProjections.isEmpty())
            {
                attributesDto = AttributeMapper
                        .attributeOfGroupProjListToDtoList(attributeProjections);
            }

            DetailedAttributeGroupResponse groupResponse = AttributeMapper
                    .attributeGroupProjToResponse(attributeGroup, attributesDto);

            groupResponseList.add(groupResponse);
        }
        return groupResponseList;

    }

    public List<AttributeOfProductResponse> getAttributesOfProduct(Integer id) {
        List<AttributeOfProjection> projections = categoryAttributeService.getAttributesOfProduct(id);
        return AttributeMapper.attributeProjectionListToAttributeOfProductResponseList(projections);
    }

    public AttributesOfProductAndCategory getAttributesOfProductAndCategory(int id) {
        Product product = productService.getById(id);

        List<AttributeOfProductResponse> productAttributes = getAttributesOfProduct(id);
        List<CompactAttributeResponse> categoryAttributes = getAllByCategoryProjection(product.getProductCategory().getId());
        List<AttributeOfProductResponse> productAttributeResponseList = checkForProdAttributesOutsideCategory(categoryAttributes, productAttributes);

        return new AttributesOfProductAndCategory(product.getProductName(),
                categoryAttributes,
                productAttributeResponseList);

    }

    @NotNull
    private List<AttributeOfProductResponse> checkForProdAttributesOutsideCategory(List<CompactAttributeResponse> categoryAttributes, List<AttributeOfProductResponse> productAttributes) {
        List<AttributeOfProductResponse> productAttributeResponseList = new ArrayList<>();

        Set<Integer> allowedAttributeIds = categoryAttributes
                .stream()
                .map(CompactAttributeResponse::attributeNameId)
                .collect(Collectors.toSet());

        for (AttributeOfProductResponse attribute : productAttributes) {
            Boolean allowed = allowedAttributeIds.contains(attribute.attributeNameId());

            AttributeOfProductResponse attributeOfProductResponse = new AttributeOfProductResponse(
                    attribute.attributeNameId(),
                    attribute.attributeName(),
                    attribute.attributeValue(),
                    attribute.measurementUnit(),
                    allowed
            );
            productAttributeResponseList.add(attributeOfProductResponse);
        }
        return productAttributeResponseList;
    }

    private List<CompactAttributeResponse> getAllByCategoryProjection(int id) {
        List<AttributeOfProjection> attributeGroups = attributeGroupService.getAttributesOfCategory(id);
        return AttributeMapper.projectionListToCompactResponseList(attributeGroups);
    }
}
