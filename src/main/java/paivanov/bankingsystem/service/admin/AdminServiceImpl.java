package paivanov.bankingsystem.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import paivanov.bankingsystem.dto.AccountDto;
import paivanov.bankingsystem.dto.ClientDto;
import paivanov.bankingsystem.model.Account;
import paivanov.bankingsystem.model.Client;
import paivanov.bankingsystem.model.Currency;
import paivanov.bankingsystem.repository.AccountRepository;
import paivanov.bankingsystem.repository.ClientRepository;
import paivanov.bankingsystem.service.exceptions.EntityNotFoundException;
import paivanov.bankingsystem.service.utils.AccountMapper;
import paivanov.bankingsystem.service.utils.ClientMapper;
import paivanov.bankingsystem.service.utils.PasswordHasher;

@Service
public class AdminServiceImpl implements AdminService {

    private final ClientRepository clientRepository;

    private final AccountRepository accountRepository;

    @Autowired
    public AdminServiceImpl(ClientRepository clientRepository, AccountRepository accountRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ClientDto createClient(String name, String password) {
        Client client = new Client(name, PasswordHasher.getHash(password));
        clientRepository.save(client);
        return ClientMapper.fromEntity(client);
    }

    @Override
    public AccountDto createAccount(Currency currency, Integer balance, Integer clientId) throws Exception {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        Account account = new Account(currency, balance, client);
        accountRepository.save(account);
        return AccountMapper.fromEntity(account);
    }
}
