package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String message) {
        super(message);
    }
}
