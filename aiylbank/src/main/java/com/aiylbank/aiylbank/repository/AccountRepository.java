package com.aiylbank.aiylbank.repository;

import com.aiylbank.aiylbank.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByNumber(String number);

    List<Account> findByBalanceLessThan(BigDecimal balance);
}

