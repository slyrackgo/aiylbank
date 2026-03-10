package com.aiylbank.aiylbank.controller;

import com.aiylbank.aiylbank.dto.AccountDto;
import com.aiylbank.aiylbank.dto.TransferRequestDto;
import com.aiylbank.aiylbank.dto.TransferResponseDto;
import com.aiylbank.aiylbank.dto.AccountStatementItemDto;
import com.aiylbank.aiylbank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // Основная операция перевода средств между счетами банка.
    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDto> createTransfer(@Valid @RequestBody TransferRequestDto request) {
        TransferResponseDto response = transferService.performTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Получить список всех переводов за последний месяц.
    @GetMapping("/transfers/last-month")
    public List<TransferResponseDto> getTransfersForLastMonth() {
        return transferService.getTransfersForLastMonth();
    }

    // Получить список переводов за выбранный период.
    @GetMapping("/transfers")
    public List<TransferResponseDto> getTransfersForPeriod(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transferService.getTransfersForPeriod(from, to);
    }

    // Посчитать общую сумму переводов за выбранный период.
    @GetMapping("/transfers/total")
    public BigDecimal getTotalAmountForPeriod(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return transferService.getTotalAmountForPeriod(from, to);
    }

    // Найти счета с отрицательным балансом.
    @GetMapping("/accounts/negative-balance")
    public List<AccountDto> getAccountsWithNegativeBalance() {
        return transferService.getAccountsWithNegativeBalance();
    }

    // Выписка по счету с фильтрацией по дате и пагинацией.
    @GetMapping("/accounts/{accountNumber}/statement")
    public Page<AccountStatementItemDto> getAccountStatement(
            @PathVariable String accountNumber,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        return transferService.getAccountStatement(accountNumber, from, to, pageable);
    }
}

