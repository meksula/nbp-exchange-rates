package com.meksula.nbp.domain.rates.update;

import com.meksula.nbp.rates.domain.ExchangeRateEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NbpRatesUpdateIntegrationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 6, 8);
    private static final String TABLE_A_URL = "https://api.nbp.pl/api/exchangerates/tables/A/2026-06-08";
    private static final String TABLE_C_URL = "https://api.nbp.pl/api/exchangerates/tables/C/2026-06-08";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExchangeRateUpdateRepository exchangeRateUpdateRepository;

    private MockRestServiceServer mockNbpServer;

    @BeforeEach
    void setUp() {
        mockNbpServer = MockRestServiceServer.createServer(restTemplate);
        exchangeRateUpdateRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-end test: PUT /api/v1/rates with mixed batch returns correct buckets and persists updates")
    void shouldHandleMixedBatchEndToEnd() throws Exception {
        // given
        ExchangeRateEntity usdComplete = ExchangeRateEntity.builder("USD", DATE).build();
        usdComplete.setMid(new BigDecimal("3.69"));
        usdComplete.setBid(new BigDecimal("3.62"));
        usdComplete.setAsk(new BigDecimal("3.70"));

        ExchangeRateEntity eurIncomplete = ExchangeRateEntity.builder("EUR", DATE).build();
        eurIncomplete.setMid(new BigDecimal("4.24"));

        exchangeRateUpdateRepository.saveAll(List.of(usdComplete, eurIncomplete));

        // stub
        mockNbpServer.expect(requestTo(TABLE_A_URL))
                     .andExpect(method(org.springframework.http.HttpMethod.GET))
                     .andRespond(withSuccess(tableAJson(), MediaType.APPLICATION_JSON));
        mockNbpServer.expect(requestTo(TABLE_C_URL))
                     .andExpect(method(org.springframework.http.HttpMethod.GET))
                     .andRespond(withSuccess(tableCJson(), MediaType.APPLICATION_JSON));

        // when / then
        mockMvc.perform(put("/api/v1/rates")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               { "effectiveDate": "2026-06-08", "currencyCodes": ["USD", "EUR", "GBP"] }
                               """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.effectiveDate").value("2026-06-08"))
               .andExpect(jsonPath("$.ratesAlreadyUpToDate", hasSize(1)))
               .andExpect(jsonPath("$.ratesAlreadyUpToDate[0].currencyCode").value("USD"))
               .andExpect(jsonPath("$.ratesUpdated", hasSize(2)))
               .andExpect(jsonPath("$.ratesUpdated[*].currencyCode", containsInAnyOrder("EUR", "GBP")));
        mockNbpServer.verify();

        List<ExchangeRateEntity> all = exchangeRateUpdateRepository.findAll();
        assertThat(all).hasSize(3);

        ExchangeRateEntity eurAfter = findByCode(all, "EUR");
        assertThat(eurAfter.getMid()).isEqualByComparingTo("4.24");
        assertThat(eurAfter.getBid()).isEqualByComparingTo("4.19");
        assertThat(eurAfter.getAsk()).isEqualByComparingTo("4.28");

        ExchangeRateEntity gbpAfter = findByCode(all, "GBP");
        assertThat(gbpAfter.getMid()).isEqualByComparingTo("4.91");
        assertThat(gbpAfter.getBid()).isEqualByComparingTo("4.85");
        assertThat(gbpAfter.getAsk()).isEqualByComparingTo("4.95");

        ExchangeRateEntity usdAfter = findByCode(all, "USD");
        assertThat(usdAfter.getMid()).isEqualByComparingTo("3.69");
    }

    @Test
    @DisplayName("End-to-end: when all currencies are complete in DB, no NBP call is made")
    void shouldSkipNbpWhenAllAreUpToDate() throws Exception {
        // given
        ExchangeRateEntity usdComplete = ExchangeRateEntity.builder("USD", DATE).build();
        usdComplete.setMid(new BigDecimal("3.69"));
        usdComplete.setBid(new BigDecimal("3.62"));
        usdComplete.setAsk(new BigDecimal("3.70"));
        exchangeRateUpdateRepository.save(usdComplete);

        // when / then
        mockMvc.perform(put("/api/v1/rates")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               { "effectiveDate": "2026-06-08", "currencyCodes": ["USD"] }
                               """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.ratesUpdated", hasSize(0)))
               .andExpect(jsonPath("$.ratesAlreadyUpToDate[0].currencyCode").value("USD"));

        mockNbpServer.verify();
    }

    @Test
    @DisplayName("End-to-end: when NBP returns not found, returns not found to client via exception handler")
    void shouldReturn404WhenNbpHasNoData() throws Exception {
        mockNbpServer.expect(requestTo(TABLE_A_URL))
                     .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        // when / then
        mockMvc.perform(put("/api/v1/rates")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               { "effectiveDate": "2026-06-08", "currencyCodes": ["USD"] }
                               """))
               .andExpect(status().isNotFound());
    }

    private ExchangeRateEntity findByCode(List<ExchangeRateEntity> entities, String code) {
        return entities.stream()
                       .filter(e -> e.getCurrencyCode().equals(code))
                       .findFirst()
                       .orElseThrow();
    }

    private String tableAJson() {
        return """
                [
                    {
                        "table": "A",
                        "no": "108/A/NBP/2026",
                        "effectiveDate": "2026-06-08",
                        "rates": [
                            {"currency": "dolar amerykański", "code": "USD", "mid": 3.69},
                            {"currency": "euro", "code": "EUR", "mid": 4.24},
                            {"currency": "funt szterling", "code": "GBP", "mid": 4.91}
                        ]
                    }
                ]
                """;
    }

    private String tableCJson() {
        return """
                [
                    {
                        "table": "C",
                        "no": "108/C/NBP/2026",
                        "tradingDate": "2026-06-05",
                        "effectiveDate": "2026-06-08",
                        "rates": [
                            {"currency": "dolar amerykański", "code": "USD", "bid": 3.62, "ask": 3.70},
                            {"currency": "euro", "code": "EUR", "bid": 4.19, "ask": 4.28},
                            {"currency": "funt szterling", "code": "GBP", "bid": 4.85, "ask": 4.95}
                        ]
                    }
                ]
                """;
    }
}
