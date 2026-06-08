package com.meksula.nbp.rates;

import com.meksula.nbp.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);

    List<ExchangeRate> findByCurrencyCodeInAndEffectiveDate(Set<String> currencyCodes, LocalDate effectiveDate);
}
