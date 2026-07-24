package com.example.ecomerseapplication.CompositeIdClasses;

import com.example.ecomerseapplication.Entities.Product;
import com.example.ecomerseapplication.Entities.Purchase;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import java.io.Serializable;

@Embeddable
@Data
public class PurchaseCartId implements Serializable {

    @JoinColumn(name = "purchase_id")
    @ManyToOne
    Purchase purchase;

    @JoinColumn(name = "product_id")
    @ManyToOne
    Product product;
}
