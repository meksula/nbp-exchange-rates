package com.meksula.nbp.rates;

import com.meksula.nbp.domain.ExchangeRate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
class NbpRatesService {

    private final NbpRatesClient nbpRatesClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    ExchangeRate fetchCurrencyRates(String currencyCode, LocalDate effectiveDate) {

        nbpRatesClient.fetchFromTable(TableType.A, currencyCode, effectiveDate);

        throw new UnsupportedOperationException("UC1 — do napisania (task #2)");
    }
}
