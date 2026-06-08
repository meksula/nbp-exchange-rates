package com.meksula.nbp.domain.rates.api;

import com.meksula.nbp.domain.rates.ExchangeRateEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RateUpdateResponse(LocalDate effectiveDate, List<RateUpdated> ratesUpdated) {

    public static RateUpdateResponse from(LocalDate effectiveDate, List<ExchangeRateEntity> exchangeRateEntitiesUpdated) {
        List<RateUpdated> rateUpdatedList = exchangeRateEntitiesUpdated.stream()
                                                                       .map(RateUpdated::from)
                                                                       .toList();
        return new RateUpdateResponse(effectiveDate, rateUpdatedList);
    }

    public record RateUpdated(String currencyCode, BigDecimal mid, BigDecimal bid, BigDecimal ask) {

        public static RateUpdated from(ExchangeRateEntity exchangeRateEntity) {
            return new RateUpdated(exchangeRateEntity.getCurrencyCode(), exchangeRateEntity.getMid(), exchangeRateEntity.getBid(), exchangeRateEntity.getAsk());
        }
    }
}
