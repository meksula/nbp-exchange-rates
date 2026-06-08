package com.meksula.nbp.domain.rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);

    List<ExchangeRateEntity> findByCurrencyCodeInAndEffectiveDate(Set<String> currencyCodes, LocalDate effectiveDate);
}
