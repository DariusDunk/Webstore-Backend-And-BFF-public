package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

import lombok.Getter;

@Getter
public class InvalidImageException extends RuntimeException {

    private final String type;

    public InvalidImageException(String message, String type) {
        super(message);
        this.type = type;
    }
}
