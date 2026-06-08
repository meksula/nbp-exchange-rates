package com.meksula.nbp.domain.rates;

import java.time.LocalDate;

public class RatesDataMalformedException extends RuntimeException {

    public RatesDataMalformedException(String currencyCode, LocalDate effectiveDate) {
        super("Exchange rates already retrieved are malformed for currency %s on date %s".formatted(currencyCode, effectiveDate));
    }
}