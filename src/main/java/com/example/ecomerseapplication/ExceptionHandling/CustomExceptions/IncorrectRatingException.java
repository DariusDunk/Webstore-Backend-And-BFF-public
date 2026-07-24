package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class IncorrectRatingException extends RuntimeException {
    public IncorrectRatingException(String message) {
        super(message);
    }
}
