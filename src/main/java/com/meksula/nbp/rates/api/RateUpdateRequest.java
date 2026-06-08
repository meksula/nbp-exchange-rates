package com.meksula.nbp.rates.api;

import java.time.LocalDate;
import java.util.Set;

record RateUpdateRequest(LocalDate effectiveDate, Set<String> currencyCodes) {
}
