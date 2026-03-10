package com.aiylbank.aiylbank.repository;

import com.aiylbank.aiylbank.domain.Account;
import com.aiylbank.aiylbank.domain.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    @Query("select coalesce(sum(t.amount), 0) " +
           "from Transfer t " +
           "where t.createdAt between :from and :to")
    BigDecimal sumAmountBetween(@Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to);

    @Query("select t from Transfer t " +
           "where (t.fromAccount = :account or t.toAccount = :account) " +
           "and t.createdAt between :from and :to " +
           "order by t.createdAt desc")
    Page<Transfer> findStatementForAccount(@Param("account") Account account,
                                           @Param("from") OffsetDateTime from,
                                           @Param("to") OffsetDateTime to,
                                           Pageable pageable);
}
