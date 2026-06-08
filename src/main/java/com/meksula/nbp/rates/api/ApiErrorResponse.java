package com.meksula.nbp.rates.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
record ApiErrorResponse(LocalDateTime timestamp,
                         Integer status,
                         String error,
                         String message,
                         List<FieldValidationError> fieldErrors) {

    ApiErrorResponse(LocalDateTime timestamp, Integer status, String error, String message) {
        this(timestamp, status, error, message, List.of());
    }

    record FieldValidationError(String field, String message) {
    }
}
