package paivanov.bankingsystem.service.client;

import org.springframework.stereotype.Service;
import paivanov.bankingsystem.model.Client;

@Service
public class CurrentClientManager {
    private final ThreadLocal<Integer> clientIdHolder = new ThreadLocal<>();

    public Integer getId() {
        return clientIdHolder.get();
    }

    public void login(Client client) {
        clientIdHolder.set(client.getId());
    }

    public void logout() {
        clientIdHolder.remove();
    }

    public boolean isLoggedIn() {
        return clientIdHolder.get() != null;
    }
}
