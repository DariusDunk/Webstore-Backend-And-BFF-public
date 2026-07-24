package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class LoginFailedException extends RuntimeException {
    public LoginFailedException(String message) {
        super(message);
    }
}
