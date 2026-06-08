package com.meksula.nbp.rates.api;

import com.meksula.nbp.rates.domain.NbpRatesService;
import com.meksula.nbp.rates.domain.NbpRatesUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rates")
class NbpRatesController {

    private final NbpRatesService nbpRatesService;
    private final NbpRatesUpdateService nbpRatesUpdateService;

    @GetMapping("/{currencyCode}")
    ResponseEntity<RateSummaryResponse> fetchCurrencyRates(@PathVariable String currencyCode,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) { // todo validate input!
        RateSummaryResponse rateSummaryResponse = nbpRatesService.fetchCurrencyRates(currencyCode.toUpperCase(), effectiveDate);
        return ResponseEntity.ok(rateSummaryResponse);
    }

    @PutMapping
    ResponseEntity<RateUpdateResponse> patchCurrencyRates(@RequestBody RateUpdateRequest rateUpdateRequest) {
        RateUpdateResponse rateUpdateResponse = nbpRatesUpdateService.updateCurrencyRates(rateUpdateRequest.effectiveDate(), rateUpdateRequest.currencyCodes());
        return ResponseEntity.ok(rateUpdateResponse);
    }
}
