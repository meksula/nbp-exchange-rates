package com.meksula.nbp.rates.domain;

import com.meksula.nbp.rates.api.RateUpdateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NbpRatesUpdateService {

    private final NbpRatesClientFacade nbpRatesClientFacade;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public RateUpdateResponse updateCurrencyRates(LocalDate effectiveDate, Set<String> currencyCodes) {
        Map<String, ExchangeRateEntity> existingByCode = fetchExistingByCode(effectiveDate, currencyCodes);

        List<ExchangeRateEntity> alreadyUpToDate = existingByCode.values()
                                                                  .stream()
                                                                  .filter(ExchangeRateEntity::isComplete)
                                                                  .toList();

        Set<String> codesToFetch = filterCodesNeedingFetch(currencyCodes, existingByCode);

        if (codesToFetch.isEmpty()) {
            log.info("All requested rates already up to date for effectiveDate: {}, omitting NBP API request", effectiveDate);
            return RateUpdateResponse.from(effectiveDate, List.of(), alreadyUpToDate);
        }

        List<NbpRateEntry> midRates = extractRatesOrThrow(nbpRatesClientFacade.fetchMidTableCurrencyRate(effectiveDate), effectiveDate);
        List<NbpRateEntry> bidAndAskRates = extractRatesOrThrow(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(effectiveDate), effectiveDate);

        List<ExchangeRateEntity> updated = codesToFetch.stream()
                                                       .map(currencyCode -> fillOrCreateEntity(currencyCode, effectiveDate, existingByCode.get(currencyCode), midRates, bidAndAskRates))
                                                       .flatMap(Optional::stream)
                                                       .toList();

        exchangeRateRepository.saveAll(updated);

        return RateUpdateResponse.from(effectiveDate, updated, alreadyUpToDate);
    }

    private Map<String, ExchangeRateEntity> fetchExistingByCode(LocalDate effectiveDate, Set<String> currencyCodes) {
        return exchangeRateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(effectiveDate, currencyCodes)
                                      .stream()
                                      .collect(Collectors.toMap(ExchangeRateEntity::getCurrencyCode, Function.identity()));
    }

    private Set<String> filterCodesNeedingFetch(Set<String> currencyCodes, Map<String, ExchangeRateEntity> existingByCode) {
        return currencyCodes.stream()
                             .filter(currencyCode -> !existingByCode.containsKey(currencyCode) || existingByCode.get(currencyCode).isNotComplete())
                             .collect(Collectors.toSet());
    }

    private List<NbpRateEntry> extractRatesOrThrow(List<NbpRatesResponse> responseList, LocalDate effectiveDate) {
        if (responseList.isEmpty()) {
            throw new RatesNotFoundException(effectiveDate);
        }
        NbpRatesResponse firstResponse = responseList.get(0);
        if (isNull(firstResponse) || isNull(firstResponse.getRates()) || firstResponse.getRates().isEmpty()) {
            throw new RatesDataMalformedException(effectiveDate);
        }
        return firstResponse.getRates();
    }

    private Optional<ExchangeRateEntity> fillOrCreateEntity(String currencyCode,
                                                             LocalDate effectiveDate,
                                                             ExchangeRateEntity existingEntity,
                                                             List<NbpRateEntry> midRates,
                                                             List<NbpRateEntry> bidAndAskRates) {
        Optional<NbpRateEntry> midRate = findByCode(midRates, currencyCode);
        Optional<NbpRateEntry> bidAndAskRate = findByCode(bidAndAskRates, currencyCode);

        if (midRate.isEmpty() && bidAndAskRate.isEmpty()) {
            log.info("Currency: {} not available in NBP tables for effectiveDate: {}, omitting", currencyCode, effectiveDate);
            return Optional.empty();
        }

        ExchangeRateEntity entity = nonNull(existingEntity) ? existingEntity : ExchangeRateEntity.builder(currencyCode, effectiveDate).build();

        midRate.ifPresent(rate -> entity.withMid(rate.getMid()));
        bidAndAskRate.ifPresent(rate -> entity.withBidAndAsk(rate.getBid(), rate.getAsk()));

        return Optional.of(entity);
    }

    private Optional<NbpRateEntry> findByCode(List<NbpRateEntry> rates, String currencyCode) {
        return rates.stream()
                    .filter(rate -> currencyCode.equals(rate.getCode()))
                    .findFirst();
    }
}
