package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "category_attributes", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = "products")
@ToString(exclude = {"products"})
public class CategoryAttribute {//todo trqbva da se preimenuva v bude6te CategoryAttribute ve4e ne e korektno za6toto kategoriite ve4e ne sa predstaveni tuk

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private int id;

    @JoinColumn(name = "attribute_name_id")
    @ManyToOne
    private AttributeName attributeName;

    @Column(name = "attribute_option", columnDefinition = "character varying(50)")
    private String attributeOption;

    @JsonIgnore
    @ManyToMany(mappedBy = "categoryAttributeSet",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
    fetch = FetchType.LAZY)
    private List<Product> products;
}
