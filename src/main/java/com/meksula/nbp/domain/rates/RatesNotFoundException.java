package com.meksula.nbp.domain.rates;

import java.time.LocalDate;

public class RatesNotFoundException extends RuntimeException {

    public RatesNotFoundException(String currencyCode, LocalDate effectiveDate) {
        super("No exchange rates available for currency %s on date %s".formatted(currencyCode, effectiveDate));
    }
}
