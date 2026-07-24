package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class FavouriteSizeLimitReachedException extends RuntimeException {
    public FavouriteSizeLimitReachedException(String message) {
        super(message);
    }
}
