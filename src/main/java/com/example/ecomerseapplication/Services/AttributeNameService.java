package com.example.ecomerseapplication.Services;

import com.example.ecomerseapplication.Entities.AttributeName;
import com.example.ecomerseapplication.Repositories.AttributeNameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttributeNameService {


    private final AttributeNameRepository attributeNameRepository;

    @Autowired
    public AttributeNameService(AttributeNameRepository attributeNameRepository) {
        this.attributeNameRepository = attributeNameRepository;
    }

    public List<AttributeName> getByIdsWithOptions(List<Integer> ids) {
        return attributeNameRepository.getAllByIdInWithOptions(ids);
    }

}
