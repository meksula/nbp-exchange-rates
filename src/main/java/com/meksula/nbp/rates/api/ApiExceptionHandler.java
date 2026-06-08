package com.meksula.nbp.rates.api;

import com.meksula.nbp.rates.domain.RatesDataMalformedException;
import com.meksula.nbp.rates.domain.RatesNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(RatesNotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFound(RatesNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RatesDataMalformedException.class)
    ResponseEntity<ApiErrorResponse> handleMalformedData(RatesDataMalformedException ex) {
        return errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleRequestBodyValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldValidationError> fieldErrors = ex.getBindingResult()
                                                                    .getFieldErrors()
                                                                    .stream()
                                                                    .map(fieldError -> new ApiErrorResponse.FieldValidationError(fieldError.getField(), fieldError.getDefaultMessage()))
                                                                    .toList();
        return validationErrorResponse("Request validation failed", fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiErrorResponse> handleParameterValidation(ConstraintViolationException ex) {
        List<ApiErrorResponse.FieldValidationError> fieldErrors = ex.getConstraintViolations()
                                                                    .stream()
                                                                    .map(violation -> new ApiErrorResponse.FieldValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
                                                                    .toList();
        return validationErrorResponse("API input validation failed", fieldErrors);
    }

    private ResponseEntity<ApiErrorResponse> errorResponse(HttpStatus status, String message) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status)
                             .body(apiErrorResponse);
    }

    private ResponseEntity<ApiErrorResponse> validationErrorResponse(String message, List<ApiErrorResponse.FieldValidationError> fieldErrors) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(LocalDateTime.now(),
                                                                  HttpStatus.BAD_REQUEST.value(),
                                                                  HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                                                  message,
                                                                  fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(apiErrorResponse);
    }
}
