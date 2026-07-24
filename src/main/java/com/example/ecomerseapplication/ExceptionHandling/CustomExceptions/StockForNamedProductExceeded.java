package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

import lombok.Getter;

@Getter
public class StockForNamedProductExceeded extends RuntimeException {
    private final String productName;
    private final int quantity;

    public StockForNamedProductExceeded(String message, String productName, int quantity) {
        super(message);
        this.productName = productName;
        this.quantity = quantity;
    }

}
