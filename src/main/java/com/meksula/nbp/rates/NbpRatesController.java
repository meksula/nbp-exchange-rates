package com.meksula.nbp.rates;

import lombok.RequiredArgsConstructor;
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
    ResponseEntity<?> fetchCurrencyRates(@PathVariable String currencyCode, @RequestParam LocalDate effectiveDate) {
        nbpRatesService.fetchCurrencyRates(currencyCode, effectiveDate);
        return ResponseEntity.ok()
//                             .body()
                             .build();
    }
}
