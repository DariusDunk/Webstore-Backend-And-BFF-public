package com.example.ecomerseapplication.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {

    PROCESSING("Обработва се"),
    SHIPPED("Изпратена"),
    DELIVERED("Доставена"),
    CANCELLED("Отказана"),
    REFUND_REQUESTED("Заявено връщане"),
    REFUNDED("Върната");

    private final String bgLabel;

    DeliveryStatus(String bgLabel) {
        this.bgLabel = bgLabel;
    }
}


