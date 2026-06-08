package com.meksula.nbp.domain.rates.api;

import com.meksula.nbp.domain.rates.NbpRatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rates")
class NbpRatesController {

    private final NbpRatesService nbpRatesService;

    @GetMapping("/{currencyCode}")
    ResponseEntity<RateSummaryResponse> fetchCurrencyRates(@PathVariable String currencyCode, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
        RateSummaryResponse rateSummaryResponse = nbpRatesService.fetchCurrencyRates(currencyCode.toUpperCase(), effectiveDate);
        return ResponseEntity.ok(rateSummaryResponse);
    }
}
