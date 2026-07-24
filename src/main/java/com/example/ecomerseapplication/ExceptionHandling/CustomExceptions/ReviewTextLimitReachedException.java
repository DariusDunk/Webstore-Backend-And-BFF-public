package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class ReviewTextLimitReachedException extends RuntimeException {
    public ReviewTextLimitReachedException(String message) {
        super(message);
    }
}
