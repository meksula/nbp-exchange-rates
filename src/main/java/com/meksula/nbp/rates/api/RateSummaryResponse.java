package com.meksula.nbp.rates.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.meksula.nbp.rates.domain.ExchangeRateEntity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RateSummaryResponse {

    private final String currencyCode;
    private final LocalDate effectiveDate;
    private final BigDecimal mid;
    private final BigDecimal bid;
    private final BigDecimal ask;

    public static RateSummaryResponse from(ExchangeRateEntity entity) {
        return RateSummaryResponse.builder()
                .currencyCode(entity.getCurrencyCode())
                .effectiveDate(entity.getEffectiveDate())
                .mid(entity.getMid())
                .bid(entity.getBid())
                .ask(entity.getAsk())
                .build();
    }
}
