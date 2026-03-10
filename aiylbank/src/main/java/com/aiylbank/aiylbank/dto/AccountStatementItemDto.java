package com.aiylbank.aiylbank.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AccountStatementItemDto {

    private OffsetDateTime date;
    private String operationType; // DEBIT / CREDIT
    private BigDecimal amount;
    private BigDecimal balanceAfterOperation;

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalanceAfterOperation() {
        return balanceAfterOperation;
    }

    public void setBalanceAfterOperation(BigDecimal balanceAfterOperation) {
        this.balanceAfterOperation = balanceAfterOperation;
    }
}

