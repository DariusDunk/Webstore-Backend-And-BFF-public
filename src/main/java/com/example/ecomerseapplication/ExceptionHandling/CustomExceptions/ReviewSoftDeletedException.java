package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class ReviewSoftDeletedException extends RuntimeException {
    public ReviewSoftDeletedException(String message) {
        super(message);
    }
}
