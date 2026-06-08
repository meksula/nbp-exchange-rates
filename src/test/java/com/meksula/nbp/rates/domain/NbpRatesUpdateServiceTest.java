package com.meksula.nbp.rates.domain;

import com.meksula.nbp.rates.api.RateUpdateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NbpRatesUpdateServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 8);

    @Mock
    private NbpRatesClientFacade nbpRatesClientFacade;

    @Mock
    private ExchangeRateRepository exchangeRateUpdateRepository;

    @InjectMocks
    private NbpRatesUpdateService service;

    @BeforeEach
    void setUp() {
        lenient().when(exchangeRateUpdateRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should skip NBP call and return only alreadyUpToDate when all entities are complete")
    void shouldSkipNbpCallWhenAllEntitiesAreComplete() {
        // given
        ExchangeRateEntity usd = completeEntity("USD", "3.69", "3.62", "3.70");
        ExchangeRateEntity eur = completeEntity("EUR", "4.24", "4.19", "4.28");
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("USD", "EUR")))
                .thenReturn(List.of(usd, eur));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("USD", "EUR"));

        // then
        assertThat(response.ratesUpdated()).isEmpty();
        assertThat(response.ratesAlreadyUpToDate())
                .extracting(RateUpdateResponse.RateUpdated::currencyCode)
                .containsExactlyInAnyOrder("USD", "EUR");

        verify(nbpRatesClientFacade, never()).fetchMidTableCurrencyRate(any());
        verify(nbpRatesClientFacade, never()).fetchBidAndAskTableCurrencyRate(any());
        verify(exchangeRateUpdateRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should fetch from NBP and update when entities exist but are incomplete")
    void shouldFetchAndUpdateIncompleteEntities() {
        // given
        ExchangeRateEntity usdIncomplete = incompleteEntityWithMidOnly("USD", "3.69");
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("USD")))
                .thenReturn(List.of(usdIncomplete));

        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateMid("USD", "3.69"))));
        when(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateBidAsk("USD", "3.62", "3.70"))));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("USD"));

        // then
        assertThat(response.ratesUpdated())
                .extracting(RateUpdateResponse.RateUpdated::currencyCode)
                .containsExactly("USD");
        assertThat(response.ratesUpdated().get(0).bid()).isEqualByComparingTo("3.62");
        assertThat(response.ratesUpdated().get(0).ask()).isEqualByComparingTo("3.70");
        assertThat(response.ratesAlreadyUpToDate()).isEmpty();
    }

    @Test
    @DisplayName("Should create new entity when currency requested but not in DB")
    void shouldCreateNewEntityForMissingCurrency() {
        // given
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("USD")))
                .thenReturn(List.of());

        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateMid("USD", "3.69"))));
        when(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateBidAsk("USD", "3.62", "3.70"))));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("USD"));

        // then
        ArgumentCaptor<List<ExchangeRateEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(exchangeRateUpdateRepository, times(1)).saveAll(captor.capture());

        List<ExchangeRateEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getCurrencyCode()).isEqualTo("USD");
        assertThat(saved.get(0).getEffectiveDate()).isEqualTo(DATE);
        assertThat(saved.get(0).getMid()).isEqualByComparingTo("3.69");
        assertThat(saved.get(0).getBid()).isEqualByComparingTo("3.62");
        assertThat(saved.get(0).getAsk()).isEqualByComparingTo("3.70");

        assertThat(response.ratesUpdated()).hasSize(1);
        assertThat(response.ratesAlreadyUpToDate()).isEmpty();
    }

    @Test
    @DisplayName("Should match each currency by code")
    void shouldMatchCurrenciesByCodeNotIndex() {
        // given
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("USD", "EUR")))
                .thenReturn(List.of());

        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(
                        rateMid("EUR", "4.24"),
                        rateMid("USD", "3.69")
                )));
        when(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(
                        rateBidAsk("EUR", "4.19", "4.28"),
                        rateBidAsk("USD", "3.62", "3.70")
                )));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("USD", "EUR"));

        // then
        assertThat(response.ratesUpdated()).hasSize(2);

        RateUpdateResponse.RateUpdated usdResult = findByCurrency(response.ratesUpdated(), "USD");
        assertThat(usdResult.mid()).isEqualByComparingTo("3.69");
        assertThat(usdResult.bid()).isEqualByComparingTo("3.62");
        assertThat(usdResult.ask()).isEqualByComparingTo("3.70");

        RateUpdateResponse.RateUpdated eurResult = findByCurrency(response.ratesUpdated(), "EUR");
        assertThat(eurResult.mid()).isEqualByComparingTo("4.24");
        assertThat(eurResult.bid()).isEqualByComparingTo("4.19");
        assertThat(eurResult.ask()).isEqualByComparingTo("4.28");
    }

    @Test
    @DisplayName("Should handle  complete, incomplete and missing currencies in one batch")
    void shouldHandleMixedBatch() {
        // given
        ExchangeRateEntity usdComplete = completeEntity("USD", "3.69", "3.62", "3.70");
        ExchangeRateEntity eurIncomplete = incompleteEntityWithMidOnly("EUR", "4.24");

        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("USD", "EUR", "GBP")))
                .thenReturn(List.of(usdComplete, eurIncomplete));

        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(
                        rateMid("EUR", "4.24"),
                        rateMid("GBP", "4.91")
                )));
        when(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(
                        rateBidAsk("EUR", "4.19", "4.28"),
                        rateBidAsk("GBP", "4.85", "4.95")
                )));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("USD", "EUR", "GBP"));

        // then
        assertThat(response.ratesAlreadyUpToDate())
                .extracting(RateUpdateResponse.RateUpdated::currencyCode)
                .containsExactly("USD");
        assertThat(response.ratesUpdated())
                .extracting(RateUpdateResponse.RateUpdated::currencyCode)
                .containsExactlyInAnyOrder("EUR", "GBP");
    }

    @Test
    @DisplayName("Should skip currency that is requested but not present in any NBP table")
    void shouldSkipCurrencyNotInNbpTables() {
        // given
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(DATE, Set.of("AFN", "USD")))
                .thenReturn(List.of());

        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateMid("USD", "3.69"))));
        when(nbpRatesClientFacade.fetchBidAndAskTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse(rateBidAsk("USD", "3.62", "3.70"))));

        // when
        RateUpdateResponse response = service.updateCurrencyRates(DATE, Set.of("AFN", "USD"));

        // then
        assertThat(response.ratesUpdated())
                .extracting(RateUpdateResponse.RateUpdated::currencyCode)
                .containsExactly("USD");
        assertThat(response.ratesAlreadyUpToDate()).isEmpty();
    }

    @Test
    @DisplayName("Should throw RatesNotFoundException when NBP returns empty list - data malformed or not published")
    void shouldThrowWhenNbpReturnsEmptyList() {
        // given
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(any(), anySet()))
                .thenReturn(List.of());
        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of()); // weekend — no data

        // when / then
        assertThatThrownBy(() -> service.updateCurrencyRates(DATE, Set.of("USD")))
                .isInstanceOf(RatesNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw RatesDataMalformedException when NBP returns response with empty rates list")
    void shouldThrowWhenNbpReturnsMalformedResponse() {
        // given
        when(exchangeRateUpdateRepository.findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(any(), anySet()))
                .thenReturn(List.of());
        when(nbpRatesClientFacade.fetchMidTableCurrencyRate(DATE))
                .thenReturn(List.of(tableResponse()));

        // when / then
        assertThatThrownBy(() -> service.updateCurrencyRates(DATE, Set.of("USD")))
                .isInstanceOf(RatesDataMalformedException.class);
    }

    private ExchangeRateEntity completeEntity(String code, String mid, String bid, String ask) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder(code, DATE).build();
        entity.setMid(new BigDecimal(mid));
        entity.setBid(new BigDecimal(bid));
        entity.setAsk(new BigDecimal(ask));
        return entity;
    }

    private ExchangeRateEntity incompleteEntityWithMidOnly(String code, String mid) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder(code, DATE).build();
        entity.setMid(new BigDecimal(mid));
        return entity;
    }

    private NbpRatesResponse tableResponse(NbpRateEntry... entries) {
        NbpRatesResponse response = new NbpRatesResponse();
        response.setRates(List.of(entries));
        return response;
    }

    private NbpRateEntry rateMid(String code, String mid) {
        NbpRateEntry entry = new NbpRateEntry();
        entry.setCode(code);
        entry.setMid(new BigDecimal(mid));
        return entry;
    }

    private NbpRateEntry rateBidAsk(String code, String bid, String ask) {
        NbpRateEntry entry = new NbpRateEntry();
        entry.setCode(code);
        entry.setBid(new BigDecimal(bid));
        entry.setAsk(new BigDecimal(ask));
        return entry;
    }

    private RateUpdateResponse.RateUpdated findByCurrency(List<RateUpdateResponse.RateUpdated> list, String code) {
        return list.stream()
                   .filter(r -> r.currencyCode().equals(code))
                   .findFirst()
                   .orElseThrow();
    }
}
