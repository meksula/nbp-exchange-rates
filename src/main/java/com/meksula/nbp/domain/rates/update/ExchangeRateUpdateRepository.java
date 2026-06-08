package com.meksula.nbp.domain.rates.update;

import com.meksula.nbp.domain.rates.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
interface ExchangeRateUpdateRepository extends JpaRepository<ExchangeRateEntity, Long> {

    @Query("SELECT e FROM ExchangeRateEntity e WHERE e.effectiveDate = :effectiveDate AND e.currencyCode IN :currencyCodes")
    List<ExchangeRateEntity> findAllCurrencyRatesByEffectiveDateAndCurrencyCodes(LocalDate effectiveDate, Set<String> currencyCodes);
}
