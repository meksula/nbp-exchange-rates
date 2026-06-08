package com.meksula.nbp.domain.rates;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
class NbpRatesClient {

    private final RestTemplate restTemplate;
    private final String rootUrl;

    NbpRatesClient(RestTemplate restTemplate, @Value("${nbp-api.root-url}") String rootUrl) {
        this.restTemplate = restTemplate;
        this.rootUrl = rootUrl;
    }

    @Retry(name = "nbp-api-retry")
    public Optional<NbpRatesResponse> fetchCurrencyRate(TableType table, String currencyCode, LocalDate effectiveDate) {
        final URI uri = prepareUri(table, currencyCode, effectiveDate);
        try {
            ResponseEntity<NbpRatesResponse> response = restTemplate.getForEntity(uri, NbpRatesResponse.class);
            log.info("NBP API response code: {}, for URI: {}", response.getStatusCode(), uri);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound notFound) {
            log.info("NBP API resource not found for URI: {}", uri);
            return Optional.empty();
        } catch (HttpClientErrorException clientError) {
            log.warn("NBP client error with response code: {} for URI: {} — not retrying", clientError.getStatusCode(), uri);
            return Optional.empty();
        }
    }

    private URI prepareUri(TableType table, String currencyCode, LocalDate date) {
        return UriComponentsBuilder.fromHttpUrl(rootUrl)
                                   .pathSegment("exchangerates", "rates", table.name(), currencyCode, date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                   .build()
                                   .toUri();
    }
}
