package paivanov.bankingsystem.service.client;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import paivanov.bankingsystem.dto.AccountDto;
import paivanov.bankingsystem.model.Account;
import paivanov.bankingsystem.model.Client;
import paivanov.bankingsystem.repository.AccountRepository;
import paivanov.bankingsystem.repository.ClientRepository;
import paivanov.bankingsystem.service.exceptions.*;
import paivanov.bankingsystem.service.utils.AccountMapper;
import paivanov.bankingsystem.service.utils.PasswordHasher;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final AccountRepository accountRepository;

    private final CurrentClientManager currentClientManager;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.currentClientManager = new CurrentClientManager();
    }

    @Override
    public void login(Integer id, String password) throws Exception {
        Client client = getClientById(id);

        Long hashedPassword = PasswordHasher.getHash(password);
        if (!client.getHashedPassword().equals(hashedPassword)) {
            throw new AuthorizationException("Wrong password");
        }

        currentClientManager.login(client);
    }

    @Override
    public void logout() {
        currentClientManager.logout();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void transfer(Integer fromAccountId, Integer toAccountId, Integer amount) throws Exception {
        checkClientAuthorization();
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalTransferException("Account ids can't be the same");
        }

        // Вызываем блокировки в порядке возрастания id, чтобы избежть дедлоков при множественных запросах
        Account first = getAccountByIdWithLock(Integer.min(fromAccountId, toAccountId));
        Account second = getAccountByIdWithLock(Integer.max(fromAccountId, toAccountId));

        Account fromAccount = first.getId().equals(fromAccountId) ? first : second;
        Account toAccount = first.getId().equals(fromAccountId) ? second : first;

        if (!currentClientManager.getId().equals(fromAccount.getClient().getId())) {
            throw new IllegalTransferException("Account with id " + fromAccountId + " doesn't belong to current user");
        }
        if (fromAccount.getBalance() < amount) {
            throw new IllegalTransferException("Insufficient funds");
        }
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new IllegalTransferException("Currencies don't match");
        }

        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() throws Exception {
        checkClientAuthorization();
        return accountRepository.getAllByClientId(currentClientManager.getId()).stream()
            .map(AccountMapper::fromEntity)
            .toList();
    }

    private void checkClientAuthorization() throws ClientNotAuthorizedException {
        if (!currentClientManager.isLoggedIn()) {
            throw new ClientNotAuthorizedException("Client is not logged in");
        }
    }

    private Account getAccountByIdWithLock(Integer id) throws EntityNotFoundException {
        Optional<Account> optionalAccount = accountRepository.findByIdWithLock(id);
        if (optionalAccount.isEmpty()) {
            throw new EntityNotFoundException("Account with id " + id + " not found");
        }
        return optionalAccount.get();
    }

    private Client getClientById(Integer id) throws EntityNotFoundException {
        Optional<Client> optionalClient = clientRepository.findById(id);
        if (optionalClient.isEmpty()) {
            throw new EntityNotFoundException("Client with id " + id + " not found");
        }
        return optionalClient.get();
    }
}
