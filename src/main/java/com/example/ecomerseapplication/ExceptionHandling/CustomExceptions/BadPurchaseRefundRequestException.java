package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

import lombok.Getter;

@Getter
public class BadPurchaseRefundRequestException extends RuntimeException {

    private final String title;
    private final String detail;

    public BadPurchaseRefundRequestException(String message, String title, String detail) {
        super(message);
        this.title = title;
        this.detail = detail;
    }
}
