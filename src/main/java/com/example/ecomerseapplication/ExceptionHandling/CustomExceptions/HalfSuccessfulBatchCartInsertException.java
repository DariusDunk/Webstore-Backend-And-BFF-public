package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class HalfSuccessfulBatchCartInsertException extends RuntimeException {
    public HalfSuccessfulBatchCartInsertException(String message) {
        super(message);
    }
}
