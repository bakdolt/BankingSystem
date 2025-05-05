package paivanov.bankingsystem.dto;

import lombok.Builder;
import lombok.Getter;
import paivanov.bankingsystem.model.Currency;

@Builder
@Getter
public class AccountDto {

    private final Integer id;
    private final Currency currency;
    private final Integer balance;
    private final Integer clientId;

    @Override
    public String toString() {
        return "{account id: " + id + ", currency: " + currency + ", balance: " + balance + "}";
    }
}
