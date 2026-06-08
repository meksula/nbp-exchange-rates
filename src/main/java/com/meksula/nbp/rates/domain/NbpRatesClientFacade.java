package com.meksula.nbp.rates.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class NbpRatesClientFacade {

    private final NbpRatesClient nbpRatesClient;

    public Optional<NbpRatesResponse> fetchMidCurrencyRate(String currencyCode, LocalDate effectiveDate) {
        return nbpRatesClient.fetchCurrencyRate(TableType.A, currencyCode, effectiveDate);
    }

    public Optional<NbpRatesResponse> fetchBidAndAskCurrencyRate(String currencyCode, LocalDate effectiveDate) {
        return nbpRatesClient.fetchCurrencyRate(TableType.C, currencyCode, effectiveDate);
    }

    public List<NbpRatesResponse> fetchMidTableCurrencyRate(LocalDate effectiveDate) {
        return nbpRatesClient.fetchTableCurrencyRate(TableType.A, effectiveDate);
    }

    public List<NbpRatesResponse> fetchBidAndAskTableCurrencyRate(LocalDate effectiveDate) {
        return nbpRatesClient.fetchTableCurrencyRate(TableType.C, effectiveDate);
    }

}
