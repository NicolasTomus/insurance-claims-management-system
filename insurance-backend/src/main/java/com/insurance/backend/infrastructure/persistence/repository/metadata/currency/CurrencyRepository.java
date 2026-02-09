package com.insurance.backend.infrastructure.persistence.repository.metadata.currency;

import com.insurance.backend.domain.metadata.currency.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    boolean existsByCode(String code);
}
