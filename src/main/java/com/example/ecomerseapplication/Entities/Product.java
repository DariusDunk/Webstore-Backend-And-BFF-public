package com.example.ecomerseapplication.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "products", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = {"productImages", "categoryAttributeSet", "saleProducts"})
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Size(max = 500, message = "Product name exceeds the 500 characters limit")
    @Column(name = "product_name")
    private String productName;

    @JoinColumn(name = "product_category_id")
    @ManyToOne
    private ProductCategory productCategory;

    @Column(name = "original_price_stotinki")
    private int originalPriceStotinki;

    @Column(name = "product_code", columnDefinition = "character varying(10)")
    private String productCode;

    @Column(name = "quantity_in_stock")
    private int quantityInStock;

    @JoinColumn(name = "manufacturer_id")
    @ManyToOne
    private Manufacturer manufacturer;

    @Column(name = "product_description")
    private String productDescription;

    private short rating;

    @Column(name = "delivery_cost", columnDefinition = "smallint DEFAULT 0")
    private short deliveryCost;

    @Column(columnDefinition = "character varying(30)")
    private String model;

    @JoinTable(name = "product_attributes", schema = "online_shop",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id"))
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<CategoryAttribute> categoryAttributeSet;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @OneToMany(mappedBy = "product",
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH},
    orphanRemoval = true)
    private List<ProductImage> productImages;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "product")
    private List<Review> reviews;

    @Column(name = "review_count")
    private int reviewCount;

    @CreationTimestamp
    @Column(name = "added_at")
    private Instant creationTimeStamp;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<SaleProduct> saleProducts;

    public boolean isInStock() {
        return quantityInStock > 0;
    }


    public Optional<SaleProduct> getMainSaleProduct() {
        if (saleProducts == null || saleProducts.isEmpty()) {
            return Optional.empty();
        }

        return saleProducts.stream().filter(SaleProduct::getIsMain).findFirst();
    }

    public void updateProduct(String productName,
                              int originalPriceStotinki,
                              String productDescription,
                              ProductCategory category,
                              String productCode,
                              int quantityInStock,
                              Manufacturer manufacturer,
                              String model) {
        this.productName = productName;
        this.originalPriceStotinki = originalPriceStotinki;
        this.productDescription = productDescription.trim();
        this.productCategory = category;
        this.productCode = productCode;
        this.quantityInStock = quantityInStock;
        this.manufacturer = manufacturer;
        this.model = model;
    }

}
