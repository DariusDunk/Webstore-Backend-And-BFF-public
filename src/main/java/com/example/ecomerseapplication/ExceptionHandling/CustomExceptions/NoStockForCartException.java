package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class NoStockForCartException extends RuntimeException {
    public NoStockForCartException(String message) {
        super(message);
    }
}
