package com.meksula.nbp.rates.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

record RateUpdateRequest(@NotNull @PastOrPresent LocalDate effectiveDate,
                         @NotNull @Size(min = 1, max = 60) Set<@Pattern(regexp = "[A-Z]{3}", message = "Currency code must be 3 uppercase letters (ISO 4217)") String> currencyCodes) {
}
