package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class InvalidPurchaseActionException extends RuntimeException {
    public InvalidPurchaseActionException(String message) {
        super(message);
    }
}
