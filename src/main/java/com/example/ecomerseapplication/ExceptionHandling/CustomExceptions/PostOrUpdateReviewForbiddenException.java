package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class PostOrUpdateReviewForbiddenException extends RuntimeException {
    public PostOrUpdateReviewForbiddenException(String message) {
        super(message);
    }
}
