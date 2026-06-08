package com.meksula.nbp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate", uniqueConstraints = @UniqueConstraint(name = "uk_exchange_rate_currency_date", columnNames = {"currency_code", "effective_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"currencyCode", "effectiveDate"})
@ToString
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "mid_rate", precision = 19, scale = 6)
    private BigDecimal mid;

    @Column(name = "bid_rate", precision = 19, scale = 6)
    private BigDecimal bid;

    @Column(name = "ask_rate", precision = 19, scale = 6)
    private BigDecimal ask;
}
