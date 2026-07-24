package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "product_categories", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = {
//        "categoryAttributes",
        "products"})
@ToString(exclude = {"products",
//        "categoryAttributes",
        "attributeGroups"} )
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_category_id")
    private int id;

    @Column(name = "category_name", columnDefinition = "character varying(30)")
    private String categoryName;

    @Column(name = "category_image")
    private String categoryImage;


    @JoinColumn(name = "parent_category_id")
    @ManyToOne
    private ProductCategory parentCategory;

    @JsonIgnore
    @OneToMany(mappedBy = "productCategory",
            cascade = {CascadeType.DETACH,CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<Product> products;

//    @JsonIgnore
//    @OneToMany(cascade = {CascadeType.DETACH,CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH},
//            mappedBy = "productCategory")
//    private List<CategoryAttribute> categoryAttributes;

    @ManyToMany
    @JoinTable(name = "attribute_groups_of_category", schema = "online_shop",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "attr_group_id"))
    private List<AttributeGroup> attributeGroups;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    public void updateCategory(String name, List<AttributeGroup> attributeGroups, Boolean isDeleted){
        this.categoryName = name;
        this.attributeGroups = attributeGroups;
        this.isDeleted = isDeleted;
    }

}
