package com.example.ecomerseapplication.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images", schema = "online_shop")
@Data
@EqualsAndHashCode(exclude = "product")
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private int id;

    @Column(name = "image_filename")
    private String imageFileName;

    @JsonIgnore
    @JoinColumn(name = "product_id")
    @ManyToOne
    private Product product;

    public ProductImage(String imageFileName, Product product) {
        this.imageFileName = imageFileName;
        this.product = product;
    }
}
