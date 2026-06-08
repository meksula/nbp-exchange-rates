package com.meksula.nbp.domain.rates.update;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meksula.nbp.domain.rates.ExchangeRateEntity;
import com.meksula.nbp.domain.rates.NbpRatesClientFacade;
import com.meksula.nbp.domain.rates.NbpRatesResponse;
import com.meksula.nbp.domain.rates.RatesDataMalformedException;
import com.meksula.nbp.domain.rates.api.RateUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NbpRatesUpdateService {

    private final NbpRatesClientFacade nbpRatesClientFacade;
    private final ExchangeRateUpdateRepository exchangeRateUpdateRepository;

    @Transactional
    public RateUpdateResponse updateCurrencyRates(LocalDate effectiveDate, Set<String> currencyCodes) {
        List<ExchangeRateEntity> exchangeRateEntities = exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(effectiveDate, currencyCodes);
        List<ExchangeRateEntity> exchangeRateEntitiesNotCompleted = filterNotCompletedEntities(exchangeRateEntities);

        List<NbpRatesResponse> nbpMidRatesResponse = nbpRatesClientFacade.fetchMidTableCurrencyRate(effectiveDate);
        List<NbpRatesResponse> nbpBidAndAskRatesResponse = nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(effectiveDate);

        validateResponses(nbpMidRatesResponse, nbpBidAndAskRatesResponse, effectiveDate);

        List<ExchangeRateEntity> exchangeRateEntitiesUpdated = fillEntityMissingData(exchangeRateEntitiesNotCompleted, nbpMidRatesResponse, nbpBidAndAskRatesResponse);
        exchangeRateUpdateRepository.saveAll(exchangeRateEntitiesUpdated);

        return RateUpdateResponse.from(effectiveDate, exchangeRateEntitiesUpdated);
    }

    private void validateResponses(List<NbpRatesResponse> nbpMidRatesResponse, List<NbpRatesResponse> nbpBidAndAskRatesResponse, LocalDate effectiveDate) {
        if (nbpMidRatesResponse.isEmpty() || nbpBidAndAskRatesResponse.isEmpty()) {
            throw new RatesDataMalformedException(effectiveDate);
        }
    }

    private List<ExchangeRateEntity> filterNotCompletedEntities(List<ExchangeRateEntity> exchangeRateEntities) {
        return exchangeRateEntities.stream()
                                   .filter(ExchangeRateEntity::isNotComplete)
                                   .toList();
    }

    private List<ExchangeRateEntity> fillEntityMissingData(List<ExchangeRateEntity> exchangeRateEntitiesNotCompleted, List<NbpRatesResponse> nbpMidRatesResponse, List<NbpRatesResponse> nbpBidAndAskRatesResponse) {



        return List.of(); // todo temporary
    }
}
