package com.meksula.nbp.rates.api;

import com.meksula.nbp.rates.domain.ExchangeRateEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RateUpdateResponse(LocalDate effectiveDate,
                                  List<RateUpdated> ratesUpdated,
                                  List<RateUpdated> ratesAlreadyUpToDate) {

    public static RateUpdateResponse from(LocalDate effectiveDate,
                                          List<ExchangeRateEntity> updated,
                                          List<ExchangeRateEntity> alreadyUpToDate) {
        List<RateUpdated> updatedMapped = updated.stream()
                                                 .map(RateUpdated::from)
                                                 .toList();
        List<RateUpdated> alreadyUpToDateMapped = alreadyUpToDate.stream()
                                                                 .map(RateUpdated::from)
                                                                 .toList();
        return new RateUpdateResponse(effectiveDate, updatedMapped, alreadyUpToDateMapped);
    }

    public record RateUpdated(String currencyCode, BigDecimal mid, BigDecimal bid, BigDecimal ask) {

        public static RateUpdated from(ExchangeRateEntity exchangeRateEntity) {
            return new RateUpdated(exchangeRateEntity.getCurrencyCode(),
                                   exchangeRateEntity.getMid(),
                                   exchangeRateEntity.getBid(),
                                   exchangeRateEntity.getAsk());
        }
    }
}
