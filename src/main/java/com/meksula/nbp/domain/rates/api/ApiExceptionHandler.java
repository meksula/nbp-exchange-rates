package com.meksula.nbp.domain.rates.api;

import com.meksula.nbp.domain.rates.RatesNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(RatesNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(RatesNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiErrorResponse> handleValidation(Exception ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status)
                             .body(apiErrorResponse);
    }
}
