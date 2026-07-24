package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class DuplicatedAttributeException extends RuntimeException {
    public DuplicatedAttributeException(String message) {
        super(message);
    }
}
