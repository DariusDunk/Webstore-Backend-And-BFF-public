package com.example.ecomerseapplication.Entities;

import com.example.ecomerseapplication.CompositeIdClasses.PurchaseCartId;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_carts", schema = "online_shop")
@Data
@NoArgsConstructor
public class PurchaseCart {

    @EmbeddedId
    private PurchaseCartId purchaseCartId =new PurchaseCartId();

    private short quantity;

    @Column(name = "single_price")
    private int singlePrice;

    public PurchaseCart(PurchaseCartId id, short quantity) {
        this.purchaseCartId = id;
        this.quantity = quantity;
    }

    public PurchaseCart(PurchaseCartId purchaseCartId, short quantity, int singlePrice) {
        this.purchaseCartId = purchaseCartId;
        this.quantity = quantity;
        this.singlePrice = singlePrice;
    }
}
