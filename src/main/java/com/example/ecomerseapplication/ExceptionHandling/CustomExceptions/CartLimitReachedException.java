package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class CartLimitReachedException extends RuntimeException {
    public CartLimitReachedException(String message) {
        super(message);
    }
}
