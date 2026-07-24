package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class ProductAlreadyInFavouritesException extends RuntimeException {
    public ProductAlreadyInFavouritesException(String message) {
        super(message);
    }
}
