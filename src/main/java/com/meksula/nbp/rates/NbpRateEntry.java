package com.meksula.nbp.rates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class NbpRateEntry {

    private String no;
    private LocalDate effectiveDate;
    private BigDecimal mid;
    private BigDecimal bid;
    private BigDecimal ask;
}
