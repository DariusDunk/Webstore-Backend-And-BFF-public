package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class NoCategoryAndManufacturerPresentException extends RuntimeException {
    public NoCategoryAndManufacturerPresentException(String message) {
        super(message);
    }
}
