package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

import lombok.Getter;

@Getter
public class InvalidEnumNameException extends RuntimeException {

    private final String title;
    private final String detail;

    public InvalidEnumNameException(String message, String title, String detail) {
        super(message);
        this.title = title;
        this.detail = detail;
    }
}
