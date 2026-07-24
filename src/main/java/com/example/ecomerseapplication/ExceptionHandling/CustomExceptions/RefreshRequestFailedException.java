package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class RefreshRequestFailedException extends RuntimeException {
    public RefreshRequestFailedException(String message) {
        super(message);
    }

    public RefreshRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
