package com.example.ecomerseapplication.enums;

import lombok.Getter;

import java.util.Objects;

public enum ProductSortType {
    RELEVANCE("relevance"),
    POPULARITY("popularity"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    NEWEST("newest"),
    REVIEW_COUNT("review_count"),
    PRODUCT_CODE("product_code"),
    PRODUCT_ID("product_id_asc");

    @Getter
    private final String value;

    public static boolean isValid(String value) {
        value = value.toLowerCase();
        return Objects.equals(value, RELEVANCE.getValue())
                || Objects.equals(value, POPULARITY.getValue())
                || Objects.equals(value, PRICE_ASC.getValue())
                || Objects.equals(value, PRICE_DESC.getValue())
                || Objects.equals(value, NEWEST.getValue())
                || Objects.equals(value, REVIEW_COUNT.getValue())
                || Objects.equals(value, PRODUCT_CODE.getValue());
    }

    ProductSortType(String value) {
        this.value = value;
    }
}
