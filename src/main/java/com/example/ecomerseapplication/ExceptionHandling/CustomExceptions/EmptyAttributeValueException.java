package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class EmptyAttributeValueException extends RuntimeException {
    public EmptyAttributeValueException(String message) {
        super(message);
    }
}
