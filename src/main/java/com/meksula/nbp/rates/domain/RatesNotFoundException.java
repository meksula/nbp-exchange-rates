package com.meksula.nbp.rates.domain;

import java.time.LocalDate;

public class RatesNotFoundException extends RuntimeException {

    public RatesNotFoundException(String currencyCode, LocalDate effectiveDate) {
        super("No exchange rates available for currency %s on date %s".formatted(currencyCode, effectiveDate));
    }

    public RatesNotFoundException(LocalDate effectiveDate) {
        super("No exchange rates published by NBP on date %s".formatted(effectiveDate));
    }
}
