package com.meksula.nbp.domain.rates;

import java.time.LocalDate;

class RatesNotFoundException extends RuntimeException {

    RatesNotFoundException(String currencyCode, LocalDate effectiveDate) {
        super("No exchange rates available for currency %s on date %s".formatted(currencyCode, effectiveDate));
    }
}
