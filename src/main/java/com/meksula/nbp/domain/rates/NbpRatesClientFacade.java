package com.meksula.nbp.domain.rates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class NbpRatesClientFacade {

    private final NbpRatesClient nbpRatesClient;

    Optional<NbpRatesResponse> fetchMidCurrencyRate(String currencyCode, LocalDate effectiveDate) {
        return nbpRatesClient.fetchCurrencyRate(TableType.A, currencyCode, effectiveDate);
    }

    Optional<NbpRatesResponse> fetchBidAndAskCurrencyRate(String currencyCode, LocalDate effectiveDate) {
        return nbpRatesClient.fetchCurrencyRate(TableType.C, currencyCode, effectiveDate);
    }

}
