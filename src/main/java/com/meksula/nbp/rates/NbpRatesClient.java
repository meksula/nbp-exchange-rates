package com.meksula.nbp.rates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    Optional<NbpRatesResponse> fetchFromTable(TableType table, String currencyCode, LocalDate date) {
        URI uri = UriComponentsBuilder.fromHttpUrl(rootUrl)
                .pathSegment("exchangerates", "rates", table.name(), currencyCode, date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .queryParam("format", "json")
                .build()
                .toUri();

        try {
            return Optional.ofNullable(restTemplate.getForObject(uri, NbpRatesResponse.class));
        } catch (HttpClientErrorException.NotFound e) {
            log.debug("NBP 404 for table={}, code={}, date={}", table, currencyCode, date);
            return Optional.empty();
        }
    }
}
