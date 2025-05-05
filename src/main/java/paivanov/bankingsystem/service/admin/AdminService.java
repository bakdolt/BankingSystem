package paivanov.bankingsystem.service.admin;

import paivanov.bankingsystem.dto.AccountDto;
import paivanov.bankingsystem.dto.ClientDto;
import paivanov.bankingsystem.model.Currency;

public interface AdminService {

    ClientDto createClient(String name, String password);

    AccountDto createAccount(Currency currency, Integer balance, Integer clientId) throws Exception;
}
