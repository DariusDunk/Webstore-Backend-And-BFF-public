package com.example.ecomerseapplication.enums;

import lombok.Getter;

public enum ResultTypes {
    SUCCESS("success"),
    PARTIAL_SUCCESS("partial_success"),
    ERROR("error");

    @Getter
    final String value;

    ResultTypes(String resultType) {
        this.value = resultType;
    }
}
