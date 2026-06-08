package com.meksula.nbp.domain.rates;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
interface ExchangeRateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    Optional<ExchangeRateEntity> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);
}
