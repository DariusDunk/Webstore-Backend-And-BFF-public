package com.example.ecomerseapplication.ExceptionHandling.CustomExceptions;

public class BadPurchaseCancelRequestException extends RuntimeException {
  public BadPurchaseCancelRequestException(String message) {
    super(message);
  }
}
