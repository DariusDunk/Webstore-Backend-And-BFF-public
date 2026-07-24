package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class UserIdExtractException extends RuntimeException {
    public UserIdExtractException(String message) {
        super(message);
    }
}
