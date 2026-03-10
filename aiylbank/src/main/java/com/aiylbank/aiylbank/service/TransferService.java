package com.aiylbank.aiylbank.service;

import com.aiylbank.aiylbank.domain.Account;
import com.aiylbank.aiylbank.domain.Transfer;
import com.aiylbank.aiylbank.dto.AccountDto;
import com.aiylbank.aiylbank.dto.TransferRequestDto;
import com.aiylbank.aiylbank.dto.TransferResponseDto;
import com.aiylbank.aiylbank.dto.AccountStatementItemDto;
import com.aiylbank.aiylbank.exception.InsufficientFundsException;
import com.aiylbank.aiylbank.exception.NotFoundException;
import com.aiylbank.aiylbank.repository.AccountRepository;
import com.aiylbank.aiylbank.repository.TransferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public TransferService(AccountRepository accountRepository,
                           TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public TransferResponseDto performTransfer(TransferRequestDto request) {
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Нельзя перевести средства на тот же самый счет");
        }

        Account from = accountRepository.findByNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new NotFoundException("Счет отправителя не найден: " + request.getFromAccountNumber()));
        Account to = accountRepository.findByNumber(request.getToAccountNumber())
                .orElseThrow(() -> new NotFoundException("Счет получателя не найден: " + request.getToAccountNumber()));

        BigDecimal amount = request.getAmount();
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть больше нуля");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Недостаточно средств на счете " + from.getNumber());
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        Transfer transfer = new Transfer();
        transfer.setFromAccount(from);
        transfer.setToAccount(to);
        transfer.setAmount(amount);
        transfer.setBalanceAfterFrom(from.getBalance());
        transfer.setBalanceAfterTo(to.getBalance());
        transfer.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        Transfer saved = transferRepository.save(transfer);
        return toTransferDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDto> getTransfersForLastMonth() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime monthAgo = now.minusMonths(1);
        return transferRepository.findByCreatedAtBetween(monthAgo, now)
                .stream()
                .map(this::toTransferDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDto> getTransfersForPeriod(LocalDate from, LocalDate to) {
        OffsetDateTime fromDateTime = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDateTime = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return transferRepository.findByCreatedAtBetween(fromDateTime, toDateTime)
                .stream()
                .map(this::toTransferDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountForPeriod(LocalDate from, LocalDate to) {
        OffsetDateTime fromDateTime = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDateTime = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        return transferRepository.sumAmountBetween(fromDateTime, toDateTime);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsWithNegativeBalance() {
        return accountRepository.findByBalanceLessThan(BigDecimal.ZERO)
                .stream()
                .map(this::toAccountDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AccountStatementItemDto> getAccountStatement(String accountNumber,
                                                             LocalDate from,
                                                             LocalDate to,
                                                             Pageable pageable) {
        Account account = accountRepository.findByNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Счет не найден: " + accountNumber));

        OffsetDateTime fromDateTime = from != null
                ? from.atStartOfDay().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.MIN;
        OffsetDateTime toDateTime = to != null
                ? to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        Page<com.aiylbank.aiylbank.domain.Transfer> page =
                transferRepository.findStatementForAccount(account, fromDateTime, toDateTime, pageable);

        return page.map(t -> toStatementItemDto(t, account));
    }

    private TransferResponseDto toTransferDto(Transfer transfer) {
        TransferResponseDto dto = new TransferResponseDto();
        dto.setId(transfer.getId());
        dto.setFromAccountNumber(transfer.getFromAccount().getNumber());
        dto.setToAccountNumber(transfer.getToAccount().getNumber());
        dto.setAmount(transfer.getAmount());
        dto.setCreatedAt(transfer.getCreatedAt());
        return dto;
    }

    private AccountDto toAccountDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setNumber(account.getNumber());
        dto.setOwnerName(account.getOwnerName());
        dto.setBalance(account.getBalance());
        return dto;
    }

    private AccountStatementItemDto toStatementItemDto(Transfer transfer, Account account) {
        AccountStatementItemDto dto = new AccountStatementItemDto();
        dto.setDate(transfer.getCreatedAt());

        boolean isDebit = transfer.getFromAccount().getId().equals(account.getId());
        dto.setOperationType(isDebit ? "DEBIT" : "CREDIT");
        dto.setAmount(transfer.getAmount());

        if (isDebit) {
            dto.setBalanceAfterOperation(transfer.getBalanceAfterFrom());
        } else {
            dto.setBalanceAfterOperation(transfer.getBalanceAfterTo());
        }

        return dto;
    }
}

