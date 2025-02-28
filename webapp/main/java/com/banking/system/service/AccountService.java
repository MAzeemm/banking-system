package com.banking.system.service;

import com.banking.system.exceptions.ResourceNotFoundException;
import com.banking.system.model.Account;
import com.banking.system.model.AccountStatus;
import com.banking.system.model.Customer;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public Account createAccount(Long customerId, String accountType) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        Account account = new Account();
        account.setAccountType(accountType);
        account.setBalance(0.0);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);

        logger.info("Account created successfully for customer ID: {}", customerId);
        return accountRepository.save(account);
    }

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public void deposit(Long accountId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
        logger.info("Deposit of {} successful for account ID: {}", amount, accountId);
    }

    public void withdraw(Long accountId, Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (account.getBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
        logger.info("Withdrawal of {} successful for account ID: {}", amount, accountId);
    }

    public void closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);
        logger.info("Account with ID: {} has been closed", accountId);
    }
}
