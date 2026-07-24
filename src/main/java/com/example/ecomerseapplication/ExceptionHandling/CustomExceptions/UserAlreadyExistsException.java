package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
