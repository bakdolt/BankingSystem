package paivanov.bankingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import paivanov.bankingsystem.dto.*;
import paivanov.bankingsystem.model.Currency;
import paivanov.bankingsystem.service.admin.AdminService;
import paivanov.bankingsystem.service.client.ClientService;
import paivanov.bankingsystem.service.exceptions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BankingSystemIntegrationTests extends PostgresIntegrationTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ClientService clientService;

    private ClientDto client1;
    private ClientDto client2;
    private AccountDto client1UsdAccount;
    private AccountDto client1EurAccount;
    private AccountDto client2UsdAccount;

    @BeforeEach
    void setUp() throws Exception {
        clientService.logout();
        // Создаем клиентов
        client1 = adminService.createClient("Client1", "pass1");
        client2 = adminService.createClient("Client2", "pass2");

        // Создаем счета
        client1UsdAccount = adminService.createAccount(Currency.USD, 1000, client1.getId());
        client1EurAccount = adminService.createAccount(Currency.EUR, 1000, client1.getId());
        client2UsdAccount = adminService.createAccount(Currency.USD, 1000, client2.getId());
    }

    @Test
    void testSuccessfulTransfer() throws Exception {
        // Логинимся как первый клиент
        clientService.login(client1.getId(), "pass1");

        // Клиент 1 переводит 500 долларов на счет второго клиента
        clientService.transfer(client1UsdAccount.getId(), client2UsdAccount.getId(), 500);

        // Проверяем что на балансе 1го клиента осталось 500 долларов
        List<AccountDto> accounts = clientService.getAllAccounts();
        AccountDto fromAccount = accounts.stream()
            .filter(a -> a.getId().equals(client1UsdAccount.getId()))
            .findFirst()
            .orElseThrow();
        assertEquals(500, fromAccount.getBalance());

        // Логинимся как второй клиент для проверки
        clientService.logout();
        clientService.login(client2.getId(), "pass2");

        // Проверяем что баланс 2го клиента увеличился на 500 долларов
        accounts = clientService.getAllAccounts();
        AccountDto toAccount = accounts.stream()
            .filter(a -> a.getId().equals(client2UsdAccount.getId()))
            .findFirst()
            .orElseThrow();
        assertEquals(1500, toAccount.getBalance());
    }

    @Test
    void testTransferToSameAccount() throws Exception {
        clientService.login(client1.getId(), "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(client1UsdAccount.getId(), client1UsdAccount.getId(), 100)
        );
    }

    @Test
    void testTransferDifferentCurrencies() throws Exception {
        clientService.login(client1.getId(), "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(client1UsdAccount.getId(), client1EurAccount.getId(), 100)
        );
    }

    @Test
    void testTransferFromOtherClientAccount() throws Exception {
        clientService.login(client1.getId(), "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(client2UsdAccount.getId(), client1UsdAccount.getId(), 100)
        );
    }

    @Test
    void testTransferInsufficientFunds() throws Exception {
        clientService.login(client1.getId(), "pass1");
        assertThrows(IllegalTransferException.class, () ->
            clientService.transfer(client1UsdAccount.getId(), client2UsdAccount.getId(), 2000)
        );
    }

    @Test
    void testTransferWithoutLogin() {
        assertThrows(ClientNotAuthorizedException.class, () ->
            clientService.transfer(client1UsdAccount.getId(), client2UsdAccount.getId(), 100)
        );
    }

    @Test
    void testGetAccountsWithoutLogin() {
        assertThrows(ClientNotAuthorizedException.class, () ->
            clientService.getAllAccounts()
        );
    }

    @Test
    void testLoginWithWrongPassword() {
        assertThrows(AuthorizationException.class, () ->
            clientService.login(client1.getId(), "wrongpass")
        );
    }

    @Test
    void testCreateAccountWithNegativeBalance() {
        assertThrows(IllegalArgumentException.class, () ->
            adminService.createAccount(Currency.USD, -100, client1.getId())
        );
    }

    @Test
    void testConcurrentTransfersBetweenMultipleClients() throws Exception {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(25);
        List<Future<?>> futures = new ArrayList<>();

        ClientDto[] clients = new ClientDto[5];
        AccountDto[] accounts = new AccountDto[5];

        Integer[] expected = new Integer[5];
        for (int i = 0; i < 5; ++i) {
            expected[i] = 100000;
        }
        for (int i = 0; i < 100; ++i) {
            final int from = i % 5;
            final int to = (i + 1) % 5;
            final int amount = i + 1;
            expected[from] -= amount;
            expected[to] += amount;
        }

        // Act
        for (int i = 0; i < 5; i++) {
            clients[i] = adminService.createClient("Client" + i, "pass" + i);
            accounts[i] = adminService.createAccount(Currency.RUB, 100000, clients[i].getId());
        }

        for (int i = 0; i < 100; ++i) {
            final int from = i % 5;
            final int to = (i + 1) % 5;
            final int amount = i + 1;
            futures.add(executor.submit(() -> {
                try {
                    clientService.login(clients[from].getId(), "pass" + from);
                    clientService.transfer(accounts[from].getId(), accounts[to].getId(), amount);
                    clientService.logout();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get();
        }

        // Assert
        for (int i = 0; i < 5; i++) {
            clientService.login(clients[i].getId(), "pass" + i);
            var account = clientService.getAllAccounts().getFirst();
            assertEquals(expected[i], account.getBalance());
        }
    }
}
