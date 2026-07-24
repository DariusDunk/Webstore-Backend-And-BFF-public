package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "attribute_names", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = "categoryAttributeList")
@ToString(exclude = {"categoryAttributeList"})
public class AttributeName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_name_id")
    private int id;

    @Column(name = "attribute_name", columnDefinition = "character varying(40)")
    private String attributeName;

    @Column(name = "measurement_unit", columnDefinition = "character varying(5)")
    private String measurementUnit;

    @JsonIgnore
    @OneToMany(mappedBy = "attributeName", fetch = FetchType.LAZY)
    private List<CategoryAttribute> categoryAttributeList;

    @ManyToMany(mappedBy = "attributeNames",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    List<AttributeGroup> attributeGroups;
}
