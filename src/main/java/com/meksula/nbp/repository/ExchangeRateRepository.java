package com.meksula.nbp.repository;

import com.meksula.nbp.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByCurrencyCodeAndEffectiveDate(String currencyCode, LocalDate effectiveDate);

    List<ExchangeRate> findByCurrencyCodeInAndEffectiveDate(Collection<String> currencyCodes, LocalDate effectiveDate);
}
