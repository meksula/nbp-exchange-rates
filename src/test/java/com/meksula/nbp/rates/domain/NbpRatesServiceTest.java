package com.meksula.nbp.rates.domain;

import com.meksula.nbp.rates.api.RateSummaryResponse;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NbpRatesServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 8);
    private static final String USD = "USD";

    @Mock
    private NbpRatesClientFacade nbpRatesClientFacade;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private NbpRatesService service;

    @BeforeEach
    void setUp() {
        // save returns the input — lenient because cache-hit path skips save
        lenient().when(exchangeRateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should return cached entity without calling NBP when entity exists and is complete")
    void shouldServeFromCacheWhenEntityIsComplete() {
        // given
        ExchangeRateEntity cached = completeEntity(USD, "3.69", "3.62", "3.70");
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.of(cached));

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then
        assertThat(response.getCurrencyCode()).isEqualTo(USD);
        assertThat(response.getEffectiveDate()).isEqualTo(DATE);
        assertThat(response.getMid()).isEqualByComparingTo("3.69");
        assertThat(response.getBid()).isEqualByComparingTo("3.62");
        assertThat(response.getAsk()).isEqualByComparingTo("3.70");

        verify(nbpRatesClientFacade, never()).fetchMidCurrencyRate(any(), any());
        verify(nbpRatesClientFacade, never()).fetchBidAndAskCurrencyRate(any(), any());
        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fetch only mid from NBP when cached entity has bid/ask but no mid")
    void shouldFetchOnlyMidWhenCachedEntityIsMissingMid() {
        // given
        ExchangeRateEntity partial = entityWithBidAsk(USD, "3.62", "3.70");
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.of(partial));

        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry("3.69", null, null))));

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then
        assertThat(response.getMid()).isEqualByComparingTo("3.69");
        assertThat(response.getBid()).isEqualByComparingTo("3.62");
        assertThat(response.getAsk()).isEqualByComparingTo("3.70");

        verify(nbpRatesClientFacade, never()).fetchBidAndAskCurrencyRate(any(), any());
        verify(exchangeRateRepository).save(any());
    }

    @Test
    @DisplayName("Should fetch only bid/ask from NBP when cached entity has mid but no bid/ask")
    void shouldFetchOnlyBidAskWhenCachedEntityIsMissingBidAsk() {
        // given
        ExchangeRateEntity partial = entityWithMid(USD, "3.69");
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.of(partial));

        when(nbpRatesClientFacade.fetchBidAndAskCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry(null, "3.62", "3.70"))));

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then
        assertThat(response.getMid()).isEqualByComparingTo("3.69");
        assertThat(response.getBid()).isEqualByComparingTo("3.62");
        assertThat(response.getAsk()).isEqualByComparingTo("3.70");

        verify(nbpRatesClientFacade, never()).fetchMidCurrencyRate(any(), any());
        verify(exchangeRateRepository).save(any());
    }

    @Test
    @DisplayName("Should create new entity when nothing in DB and both NBP tables return data")
    void shouldCreateNewEntityWhenNotInDb() {
        // given
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.empty());

        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry("3.69", null, null))));
        when(nbpRatesClientFacade.fetchBidAndAskCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry(null, "3.62", "3.70"))));

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then — assert what got saved
        ArgumentCaptor<ExchangeRateEntity> captor = ArgumentCaptor.forClass(ExchangeRateEntity.class);
        verify(exchangeRateRepository).save(captor.capture());
        ExchangeRateEntity saved = captor.getValue();
        assertThat(saved.getCurrencyCode()).isEqualTo(USD);
        assertThat(saved.getEffectiveDate()).isEqualTo(DATE);
        assertThat(saved.getMid()).isEqualByComparingTo("3.69");
        assertThat(saved.getBid()).isEqualByComparingTo("3.62");
        assertThat(saved.getAsk()).isEqualByComparingTo("3.70");

        assertThat(response.getMid()).isEqualByComparingTo("3.69");
    }

    @Test
    @DisplayName("Should create partial entity (mid only) when currency is only available in mid table")
    void shouldCreatePartialEntityWhenOnlyMidAvailable() {
        // given — e.g. exotic currency present in A but not in C
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.empty());

        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry("3.69", null, null))));
        when(nbpRatesClientFacade.fetchBidAndAskCurrencyRate(USD, DATE))
                .thenReturn(Optional.empty());

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then
        assertThat(response.getMid()).isEqualByComparingTo("3.69");
        assertThat(response.getBid()).isNull();
        assertThat(response.getAsk()).isNull();
    }

    @Test
    @DisplayName("Should create partial entity (bid/ask only) when currency is only available in C table")
    void shouldCreatePartialEntityWhenOnlyBidAskAvailable() {
        // given
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.empty());

        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.empty());
        when(nbpRatesClientFacade.fetchBidAndAskCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse(rateEntry(null, "3.62", "3.70"))));

        // when
        RateSummaryResponse response = service.fetchCurrencyRates(USD, DATE);

        // then
        assertThat(response.getMid()).isNull();
        assertThat(response.getBid()).isEqualByComparingTo("3.62");
        assertThat(response.getAsk()).isEqualByComparingTo("3.70");
    }

    @Test
    @DisplayName("Should throw RatesNotFoundException when entity does not exist and both NBP tables return empty")
    void shouldThrowWhenNotInDbAndNbpReturnsNothing() {
        // given — weekend or unknown currency
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.empty());
        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.empty());
        when(nbpRatesClientFacade.fetchBidAndAskCurrencyRate(USD, DATE))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.fetchCurrencyRates(USD, DATE))
                .isInstanceOf(RatesNotFoundException.class);

        verify(exchangeRateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RatesDataMalformedException when NBP returns response with empty rates array")
    void shouldThrowWhenNbpReturnsMalformedResponse() {
        // given
        when(exchangeRateRepository.findByCurrencyCodeAndEffectiveDate(USD, DATE))
                .thenReturn(Optional.empty());
        when(nbpRatesClientFacade.fetchMidCurrencyRate(USD, DATE))
                .thenReturn(Optional.of(perCurrencyResponse())); // empty rates[]

        // when / then
        assertThatThrownBy(() -> service.fetchCurrencyRates(USD, DATE))
                .isInstanceOf(RatesDataMalformedException.class);
    }

    // ===== Helpers =====

    private ExchangeRateEntity completeEntity(String code, String mid, String bid, String ask) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder(code, DATE).build();
        entity.setMid(new BigDecimal(mid));
        entity.setBid(new BigDecimal(bid));
        entity.setAsk(new BigDecimal(ask));
        return entity;
    }

    private ExchangeRateEntity entityWithMid(String code, String mid) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder(code, DATE).build();
        entity.setMid(new BigDecimal(mid));
        return entity;
    }

    private ExchangeRateEntity entityWithBidAsk(String code, String bid, String ask) {
        ExchangeRateEntity entity = ExchangeRateEntity.builder(code, DATE).build();
        entity.setBid(new BigDecimal(bid));
        entity.setAsk(new BigDecimal(ask));
        return entity;
    }

    private NbpRatesResponse perCurrencyResponse(NbpRateEntry... entries) {
        NbpRatesResponse response = new NbpRatesResponse();
        response.setRates(List.of(entries));
        return response;
    }

    private NbpRateEntry rateEntry(String mid, String bid, String ask) {
        NbpRateEntry entry = new NbpRateEntry();
        if (mid != null) entry.setMid(new BigDecimal(mid));
        if (bid != null) entry.setBid(new BigDecimal(bid));
        if (ask != null) entry.setAsk(new BigDecimal(ask));
        return entry;
    }
}
