package java.com.banking.system;

import com.banking.system.exceptions.ResourceNotFoundException;
import com.banking.system.model.Account;
import com.banking.system.model.AccountStatus;
import com.banking.system.model.Customer;
import com.banking.system.repository.AccountRepository;
import com.banking.system.repository.CustomerRepository;
import com.banking.system.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private Account account;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);

        account = new Account();
        account.setId(100L);
        account.setAccountType("Savings");
        account.setBalance(1000.0);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);
    }

    @Test
    void testCreateAccount_Success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        Account createdAccount = accountService.createAccount(1L, "Savings");

        assertNotNull(createdAccount);
        assertEquals("Savings", createdAccount.getAccountType());
        assertEquals(0.0, createdAccount.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testCreateAccount_CustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.createAccount(1L, "Savings"));
    }

    @Test
    void testGetAccountById_Success() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));

        Optional<Account> foundAccount = accountService.getAccountById(100L);
        assertTrue(foundAccount.isPresent());
        assertEquals(100L, foundAccount.get().getId());
    }

    @Test
    void testDeposit_Success() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.deposit(100L, 500.0);

        assertEquals(1500.0, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void testDeposit_NegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> accountService.deposit(100L, -500.0));
    }

    @Test
    void testWithdraw_Success() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.withdraw(100L, 500.0);

        assertEquals(500.0, account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class, () -> accountService.withdraw(100L, 2000.0));
    }

    @Test
    void testCloseAccount_Success() {
        when(accountRepository.findById(100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.closeAccount(100L);

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        verify(accountRepository).save(account);
    }
}
