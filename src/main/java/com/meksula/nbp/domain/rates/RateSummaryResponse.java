package com.meksula.nbp.domain.rates;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class RateSummaryResponse {

    private final String currencyCode;
    private final LocalDate effectiveDate;
    private final BigDecimal mid;
    private final BigDecimal bid;
    private final BigDecimal ask;

    static RateSummaryResponse from(ExchangeRateEntity entity) {
        return RateSummaryResponse.builder()
                .currencyCode(entity.getCurrencyCode())
                .effectiveDate(entity.getEffectiveDate())
                .mid(entity.getMid())
                .bid(entity.getBid())
                .ask(entity.getAsk())
                .build();
    }
}
