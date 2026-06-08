package com.meksula.nbp.rates.domain;

import com.meksula.nbp.rates.api.RateSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static com.meksula.nbp.rates.utils.CollectionUtils.firstItemOrThrow;

@Service
@RequiredArgsConstructor
public class NbpRatesService {

    private final NbpRatesClientFacade nbpRatesClientFacade;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public RateSummaryResponse fetchCurrencyRates(String currencyCode, LocalDate effectiveDate) {
        ExchangeRateEntity exchangeRateEntity = exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(currencyCode, effectiveDate)
                                                                      .map(this::verifyDataIntegrityAndFetchMissing)
                                                                      .orElseGet(() -> fetchMissingExchangeRate(currencyCode, effectiveDate));
        return RateSummaryResponse.from(exchangeRateEntity);
    }

    private ExchangeRateEntity verifyDataIntegrityAndFetchMissing(ExchangeRateEntity entity) {
        if (entity.isComplete()) {
            return entity;
        }
        return fetchMissingRates(entity);
    }

    private ExchangeRateEntity fetchMissingRates(ExchangeRateEntity entity) {
        if (entity.hasNoMid()) {
            Optional<NbpRatesResponse> nbpRatesResponse = nbpRatesClientFacade.fetchMidCurrencyRate(entity.getCurrencyCode(), entity.getEffectiveDate());
            nbpRatesResponse.ifPresent(response -> entity.withMid(getFirstItem(response, entity.getCurrencyCode(), entity.getEffectiveDate())
                                                                          .getMid()));
        }
        if (entity.hasNoBidAndAsk()) {
            Optional<NbpRatesResponse> nbpRatesResponse = nbpRatesClientFacade.fetchBidAndAskCurrencyRate(entity.getCurrencyCode(), entity.getEffectiveDate());
            nbpRatesResponse.ifPresent(response -> {
                NbpRateEntry entry = getFirstItem(response, entity.getCurrencyCode(), entity.getEffectiveDate());
                entity.withBidAndAsk(entry.getBid(), entry.getAsk());
            });
        }
        return exchangeRateRepository.save(entity);
    }

    private ExchangeRateEntity fetchMissingExchangeRate(String currencyCode, LocalDate effectiveDate) {
        Optional<NbpRatesResponse> midRateResponse = nbpRatesClientFacade.fetchMidCurrencyRate(currencyCode, effectiveDate);
        Optional<NbpRatesResponse> bidAskRateResponse = nbpRatesClientFacade.fetchBidAndAskCurrencyRate(currencyCode, effectiveDate);

        if (midRateResponse.isEmpty() && bidAskRateResponse.isEmpty()) {
            throw new RatesNotFoundException(currencyCode, effectiveDate);
        }

        ExchangeRateEntity.ExchangeRateEntityBuilder exchangeRateEntityBuilder = ExchangeRateEntity.builder(currencyCode, effectiveDate);

        midRateResponse.ifPresent(midResponse -> {
            NbpRateEntry midNbpRateEntry = getFirstItem(midResponse, currencyCode, effectiveDate);
            exchangeRateEntityBuilder.mid(midNbpRateEntry.getMid());
        });

        bidAskRateResponse.ifPresent(bidAskResponse -> {
            NbpRateEntry bidAskNbpRateEntry = getFirstItem(bidAskResponse, currencyCode, effectiveDate);
            exchangeRateEntityBuilder.bid(bidAskNbpRateEntry.getBid())
                                     .ask(bidAskNbpRateEntry.getAsk());
        });
        ExchangeRateEntity exchangeRateEntity = exchangeRateEntityBuilder.build();

        return exchangeRateRepository.save(exchangeRateEntity);
    }

    private NbpRateEntry getFirstItem(NbpRatesResponse midResponse, String currencyCode, LocalDate effectiveDate) {
        try {
            return firstItemOrThrow(midResponse.getRates());
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new RatesDataMalformedException(currencyCode, effectiveDate);
        }
    }
}
