package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "attribute_groups", schema = "online_shop")
@Data
@ToString(exclude = "categories")
@EqualsAndHashCode(exclude = "categories")
public class AttributeGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @JoinTable(name = "attributes_of_group", schema = "online_shop",
    joinColumns = @JoinColumn(name = "attribute_group_id"),
    inverseJoinColumns = @JoinColumn(name = "attribute_name_id"))
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    List<AttributeName> attributeNames;

    @ManyToMany(mappedBy = "attributeGroups",cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
    fetch = FetchType.LAZY)
    private List<ProductCategory> categories;
}
