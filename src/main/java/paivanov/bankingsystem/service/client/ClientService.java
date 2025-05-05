package paivanov.bankingsystem.service.client;

import java.util.List;
import paivanov.bankingsystem.dto.AccountDto;

public interface ClientService {

    void login(Integer id, String password) throws Exception;

    void logout();

    void transfer(Integer fromAccountId, Integer toAccountId, Integer amount) throws Exception;

    List<AccountDto> getAllAccounts() throws Exception;
}
