package com.example.ecomerseapplication.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH_ON_DELIVERY("Наложен платеж (Плащане при доставка)");

    private final String displayNameBg;

    PaymentMethod(String displayNameBg) {
        this.displayNameBg = displayNameBg;
    }

    }
