package com.meksula.nbp.domain.rates;

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

    Optional<NbpRatesResponse> fetchFromTable(TableType table, String currencyCode, LocalDate effectiveDate) {
        URI uri = prepareUri(table, currencyCode, effectiveDate);
        try {
            return Optional.ofNullable(restTemplate.getForObject(uri, NbpRatesResponse.class));
        } catch (HttpClientErrorException e) {
            log.debug("HTTP error {} occurred for table: {}, currencyCode: {}, date: {}", e.getStatusText(), table, currencyCode, effectiveDate);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unknown error occurred: {}", e);
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
