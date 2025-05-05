package paivanov.bankingsystem;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import paivanov.bankingsystem.dto.*;
import paivanov.bankingsystem.model.*;
import paivanov.bankingsystem.repository.AccountRepository;
import paivanov.bankingsystem.repository.ClientRepository;
import paivanov.bankingsystem.service.admin.AdminService;
import paivanov.bankingsystem.service.client.ClientService;
import paivanov.bankingsystem.service.exceptions.*;
import paivanov.bankingsystem.service.utils.PasswordHasher;

@SpringBootTest
@ActiveProfiles("test")
class BankingSystemTests {

    @Autowired
    private ClientService clientService;

    @Autowired
    private AdminService adminService;

    @MockitoBean
    private ClientRepository clientRepository;

    @MockitoBean
    private AccountRepository accountRepository;

    private Client client1;
    private Client client2;
    private Account client1UsdAccount;
    private Account client1EurAccount;
    private Account client2UsdAccount;

    @BeforeEach
    void setUp() {
        clientService.logout();

        Mockito.reset(clientRepository, accountRepository);

        client1 = new Client("Client1", PasswordHasher.getHash("pass1"));
        client1.setId(1);
        client2 = new Client("Client2", PasswordHasher.getHash("pass2"));
        client2.setId(2);

        client1UsdAccount = new Account(Currency.USD, 1000, client1);
        client1UsdAccount.setId(1);
        client1EurAccount = new Account(Currency.EUR, 1000, client1);
        client1EurAccount.setId(2);
        client2UsdAccount = new Account(Currency.USD, 1000, client2);
        client2UsdAccount.setId(3);

        when(clientRepository.findById(1)).thenReturn(Optional.of(client1));
        when(clientRepository.findById(2)).thenReturn(Optional.of(client2));
        when(accountRepository.findByIdWithLock(1)).thenReturn(Optional.of(client1UsdAccount));
        when(accountRepository.findByIdWithLock(2)).thenReturn(Optional.of(client1EurAccount));
        when(accountRepository.findByIdWithLock(3)).thenReturn(Optional.of(client2UsdAccount));
    }

    @Test
    void testCreateClient() {
        when(clientRepository.save(any(Client.class))).thenReturn(client1);

        ClientDto result = adminService.createClient("Client1", "pass1");

        assertNotNull(result);
        assertEquals("Client1", result.getName());
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testCreateAccount() throws Exception {
        when(accountRepository.save(any(Account.class))).thenReturn(client1UsdAccount);

        AccountDto result = adminService.createAccount(Currency.USD, 1000, 1);

        assertNotNull(result);
        assertEquals(Currency.USD, result.getCurrency());
        assertEquals(1000, result.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void testCreateAccountWithNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () ->
            adminService.createAccount(Currency.USD, -100, 1)
        );
    }

    @Test
    void testCreateAccountForNonExistentClient() {
        when(clientRepository.findById(999)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            adminService.createAccount(Currency.USD, 1000, 999)
        );
    }

    @Test
    void testLoginSuccess() {
        assertDoesNotThrow(() -> clientService.login(1, "pass1"));
    }

    @Test
    void testLoginWrongPassword() {
        assertThrows(AuthorizationException.class, () ->
            clientService.login(1, "wrongpass")
        );
    }

    @Test
    void testLoginNonExistentClient() {
        when(clientRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            clientService.login(999, "pass1")
        );
    }

    @Test
    void testTransferSuccess() throws Exception {
        clientService.login(1, "pass1");
        clientService.transfer(1, 3, 500);

        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void testTransferWithoutLogin() {
        assertThrows(ClientNotAuthorizedException.class, () ->
            clientService.transfer(1, 3, 100)
        );
    }

    @Test
    void testTransferToSameAccount() throws Exception {
        clientService.login(1, "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(1, 1, 100)
        );
    }

    @Test
    void testTransferDifferentCurrencies() throws Exception {
        clientService.login(1, "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(1, 2, 100)
        );
    }

    @Test
    void testTransferFromOtherClientAccount() throws Exception {
        clientService.login(1, "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(3, 1, 100)
        );
    }

    @Test
    void testTransferInsufficientFunds() throws Exception {
        clientService.login(1, "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(1, 3, 2000)
        );
    }
}
