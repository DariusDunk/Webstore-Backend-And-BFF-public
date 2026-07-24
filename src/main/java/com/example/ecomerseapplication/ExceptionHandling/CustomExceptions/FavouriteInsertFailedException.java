package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class FavouriteInsertFailedException extends RuntimeException {
    public FavouriteInsertFailedException(String message) {
        super(message);
    }

    public FavouriteInsertFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
