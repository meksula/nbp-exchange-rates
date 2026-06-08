package com.meksula.nbp.domain.rates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meksula.nbp.rates.domain.NbpRateEntry;
import com.meksula.nbp.rates.domain.NbpRatesResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class NbpResponseContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("Real NBP table A response deserializes with code, currency and mid populated (no bid/ask)")
    void shouldDeserializeRealTableAResponse() throws Exception {
        String json = loadFixture("/fixtures/nbp-table-a-real.json");

        NbpRatesResponse[] parsed = objectMapper.readValue(json, NbpRatesResponse[].class);

        assertThat(parsed).hasSize(1);
        NbpRatesResponse table = parsed[0];
        assertThat(table.getTable()).isEqualTo("A");
        assertThat(table.getRates()).isNotEmpty();

        NbpRateEntry usd = findByCode(table, "USD");
        assertThat(usd.getCode()).isEqualTo("USD");
        assertThat(usd.getCurrency()).isNotBlank();
        assertThat(usd.getMid()).isNotNull();
        assertThat(usd.getBid()).isNull();
        assertThat(usd.getAsk()).isNull();
    }

    @Test
    @DisplayName("Real NBP table C response deserializes with code, currency, bid and ask populated (no mid)")
    void shouldDeserializeRealTableCResponse() throws Exception {
        String json = loadFixture("/fixtures/nbp-table-c-real.json");

        NbpRatesResponse[] parsed = objectMapper.readValue(json, NbpRatesResponse[].class);

        assertThat(parsed).hasSize(1);
        NbpRatesResponse table = parsed[0];
        assertThat(table.getTable()).isEqualTo("C");
        assertThat(table.getRates()).isNotEmpty();

        NbpRateEntry usd = findByCode(table, "USD");
        assertThat(usd.getCode()).isEqualTo("USD");
        assertThat(usd.getCurrency()).isNotBlank();
        assertThat(usd.getBid()).isNotNull();
        assertThat(usd.getAsk()).isNotNull();
        assertThat(usd.getMid()).isNull();
    }

    private NbpRateEntry findByCode(NbpRatesResponse response, String code) {
        return response.getRates().stream()
                                  .filter(rate -> code.equals(rate.getCode()))
                                  .findFirst()
                                  .orElseThrow();
    }

    private String loadFixture(String path) throws Exception {
        try (var inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Fixture not found on classpath: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
