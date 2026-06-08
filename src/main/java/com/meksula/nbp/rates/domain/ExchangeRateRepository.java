package com.meksula.nbp.rates.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);

    @Query("SELECT e FROM ExchangeRateEntity e WHERE e.effectiveDate = :effectiveDate AND e.currencyCode IN :currencyCodes")
    List<ExchangeRateEntity> findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(LocalDate effectiveDate, Set<String> currencyCodes);
}
