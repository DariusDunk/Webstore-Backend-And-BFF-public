package com.example.ecomerseapplication.CompositeIdClasses;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class SaleProductId {

    private int productId;
    private Long saleId;

}
