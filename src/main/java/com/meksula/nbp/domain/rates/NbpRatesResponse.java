package com.meksula.nbp.domain.rates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class NbpRatesResponse {

    private String table;
    private String currency;
    private String code;
    private List<NbpRateEntry> rates;
}
