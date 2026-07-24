package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class EmptyRequestException extends RuntimeException {
    public EmptyRequestException(String message) {
        super(message);
    }
}
