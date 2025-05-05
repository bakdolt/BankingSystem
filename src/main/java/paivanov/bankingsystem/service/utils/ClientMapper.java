package paivanov.bankingsystem.service.utils;

import java.util.ArrayList;
import paivanov.bankingsystem.dto.ClientDto;
import paivanov.bankingsystem.model.Client;

public class ClientMapper {

    public static ClientDto fromEntity(Client client) {
        return ClientDto.builder()
            .id(client.getId())
            .name(client.getName())
            .hashedPassword(client.getHashedPassword())
            .accounts(client.getAccounts() == null
                          ? new ArrayList<>()
                          : client.getAccounts().stream()
                .map(AccountMapper::fromEntity)
                .toList())
            .build();
    }
}
