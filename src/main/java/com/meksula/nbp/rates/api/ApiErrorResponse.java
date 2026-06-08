package com.meksula.nbp.rates.api;

import java.time.LocalDateTime;

record ApiErrorResponse(LocalDateTime timestamp, Integer status, String error, String message) {
}