package com.meksula.nbp.rates.api;

import com.meksula.nbp.rates.domain.NbpRatesService;
import com.meksula.nbp.rates.domain.NbpRatesUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/rates")
@Validated
class NbpRatesController {

    private static final String CURRENCY_CODE_REGEXP = "[A-Z]{3}";
    private static final String CURRENCY_CODE_MESSAGE = "Currency code must be 3 uppercase letters (ISO 4217)";

    private final NbpRatesService nbpRatesService;
    private final NbpRatesUpdateService nbpRatesUpdateService;

    @GetMapping("/{currencyCode}")
    ResponseEntity<RateSummaryResponse> fetchCurrencyRates(@PathVariable @Pattern(regexp = CURRENCY_CODE_REGEXP, message = CURRENCY_CODE_MESSAGE) String currencyCode,
                                                           @RequestParam @NotNull @PastOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveDate) {
        RateSummaryResponse rateSummaryResponse = nbpRatesService.fetchCurrencyRates(currencyCode, effectiveDate);
        return ResponseEntity.ok(rateSummaryResponse);
    }

    @PutMapping
    ResponseEntity<RateUpdateResponse> patchCurrencyRates(@RequestBody @Valid RateUpdateRequest rateUpdateRequest) {
        RateUpdateResponse rateUpdateResponse = nbpRatesUpdateService.updateCurrencyRates(rateUpdateRequest.effectiveDate(), rateUpdateRequest.currencyCodes());
        return ResponseEntity.ok(rateUpdateResponse);
    }
}
