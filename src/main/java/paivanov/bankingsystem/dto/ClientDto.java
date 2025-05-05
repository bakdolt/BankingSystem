package paivanov.bankingsystem.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ClientDto {

    private final Integer id;
    private final String name;
    private final Long hashedPassword;
    private final List<AccountDto> accounts;

    @Override
    public String toString() {
        return "client id: " + id + ", name: " + name + ", hashedPassword: " + hashedPassword;
    }
}
