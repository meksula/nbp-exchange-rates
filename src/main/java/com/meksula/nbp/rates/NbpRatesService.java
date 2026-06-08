package com.meksula.nbp.rates;

import com.meksula.nbp.domain.ExchangeRateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.meksula.nbp.utils.CollectionUtils.firstItemOrThrow;
import static java.util.Objects.*;

@Service
@RequiredArgsConstructor
public class NbpRatesService {

    private final NbpRatesClient nbpRatesClient;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public RateSummaryResponse fetchCurrencyRates(String currencyCode, LocalDate effectiveDate) {
        ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(currencyCode, effectiveDate)
                                                                      .map(entity -> {
                                                                          if (entity.isComplete()) {
                                                                              return entity;
                                                                          }
                                                                          return fetchMissingRates(entity);
                                                                      })
                                                                      .orElseGet(() -> fetchMissingExchangeRate(currencyCode, effectiveDate));
        return RateSummaryResponse.from(exchangeRateEntity);
    }

    private ExchangeRateEntity fetchMissingRates(ExchangeRateEntity entity) {
        if (entity.hasNoMid()) {
            Optional<NbpRatesResponse> nbpRatesResponse = nbpRatesClient.fetchFromTable(TableType.A, entity.getCurrencyCode(), entity.getEffectiveDate());
            nbpRatesResponse.ifPresent(response -> entity.withMid(firstItemOrThrow(response.getRates())
                                                                          .getMid()));
        }
        if (entity.hasNoBidAndAsk()) {
            Optional<NbpRatesResponse> nbpRatesResponse = nbpRatesClient.fetchFromTable(TableType.C, entity.getCurrencyCode(), entity.getEffectiveDate());
            nbpRatesResponse.ifPresent(response -> {
                NbpRateEntry entry = firstItemOrThrow(response.getRates());
                entity.withBidAndAsk(entry.getBid(), entry.getAsk());
            });
        }
        return exchangeRateRepository.save(entity);
    }

    private ExchangeRateEntity fetchMissingExchangeRate(String currencyCode, LocalDate effectiveDate) {
        Optional<NbpRatesResponse> aTableResponse = nbpRatesClient.fetchFromTable(TableType.A, currencyCode, effectiveDate);
        Optional<NbpRatesResponse> cTableResponse = nbpRatesClient.fetchFromTable(TableType.C, currencyCode, effectiveDate);

        ExchangeRateEntity.ExchangeRateEntityBuilder exchangeRateEntityBuilder = ExchangeRateEntity.builder(currencyCode, effectiveDate);

        aTableResponse.ifPresent(aResponse -> {
            NbpRateEntry aNbpRateEntry = firstItemOrThrow(aResponse.getRates());
            if (isNull(aNbpRateEntry)) {
                throw new RatesNotFoundException(currencyCode, effectiveDate);
            }
            exchangeRateEntityBuilder.createdDate(LocalDateTime.now())
                                     .currencyCode(aResponse.getCode())
                                     .effectiveDate(aNbpRateEntry.getEffectiveDate())
                                     .mid(aNbpRateEntry.getMid());

            cTableResponse.ifPresent(cResponse -> {
                NbpRateEntry cNbpRateEntry = firstItemOrThrow(cResponse.getRates());
                if (isNull(cNbpRateEntry)) {
                    throw new RatesNotFoundException(currencyCode, effectiveDate);
                }
                exchangeRateEntityBuilder.bid(cNbpRateEntry.getBid())
                                         .ask(cNbpRateEntry.getAsk());
            });
        });
        ExchangeRateEntity exchangeRateEntity = exchangeRateEntityBuilder.build();
        return exchangeRateRepository.save(exchangeRateEntity);
    }
}
