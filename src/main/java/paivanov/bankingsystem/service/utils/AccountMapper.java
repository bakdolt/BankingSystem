package paivanov.bankingsystem.service.utils;

import paivanov.bankingsystem.dto.AccountDto;
import paivanov.bankingsystem.model.Account;

public class AccountMapper {

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
            .id(account.getId())
            .currency(account.getCurrency())
            .balance(account.getBalance())
            .clientId(account.getClient().getId())
            .build();
    }
}
