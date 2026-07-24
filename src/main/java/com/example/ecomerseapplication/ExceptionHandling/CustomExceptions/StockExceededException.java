package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class StockExceededException extends RuntimeException {
    public StockExceededException(String message) {
        super(message);
    }
}
