package com.meksula.nbp.rates;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class NbpRatesService {

    private final NbpWebClient nbpWebClient;

    NbpCurrencyRates fetchCurrencyRates(String currencyCode, LocalDate effectiveDate) {
        return null;
    }
}
