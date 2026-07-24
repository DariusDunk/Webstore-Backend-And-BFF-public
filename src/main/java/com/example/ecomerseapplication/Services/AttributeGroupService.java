package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeGroupsWithCategoryProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.AttributeOfProjection;
import com.example.ecomerseapplication.Entities.AttributeGroup;
import com.example.ecomerseapplication.Repositories.AttributeGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeGroupService {
    private final AttributeGroupRepository attributeGroupRepository;

    public AttributeGroupService(AttributeGroupRepository attributeGroupRepository) {
        this.attributeGroupRepository = attributeGroupRepository;
    }

    public List<AttributeGroupsWithCategoryProjection> getAllWithACategory(int categoryId) {
        return attributeGroupRepository.getAllWithCategory(categoryId);
    }

    public List<AttributeGroup> getByNames(List<String> strings) {
        return attributeGroupRepository.getAttributeGroupByGroupNameIn(strings);
    }

    public List<AttributeGroup> getAll() {
        return attributeGroupRepository.findAll();
    }

    public List<AttributeOfProjection> getAllGroupsWithAttributes() {
        return attributeGroupRepository.findAllProjection();
    }

    public List<AttributeOfProjection> getByGroupId(List<Long> groupIds) {
        return attributeGroupRepository.findByGroupIdsProjection(groupIds);
    }

    public List<AttributeOfProjection> getAttributesOfCategory(int categoryId) {
        return attributeGroupRepository.getAttributesByCategoryId(categoryId);
    }
}
