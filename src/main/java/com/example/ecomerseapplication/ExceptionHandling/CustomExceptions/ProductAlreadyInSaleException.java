package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

import lombok.Getter;

@Getter
public class ProductAlreadyInSaleException extends RuntimeException {

    private final String productName;
    private final String saleName;

    public ProductAlreadyInSaleException(String message, String productName, String saleName) {
        super(message);
        this.productName = productName;
        this.saleName = saleName;
    }
}
