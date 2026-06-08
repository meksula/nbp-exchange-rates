package com.meksula.nbp.rates.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

record RateUpdateRequest(@NotNull LocalDate effectiveDate,
                         @NotNull @Size(max = 60) Set<String> currencyCodes) {
}
