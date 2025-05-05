package paivanov.bankingsystem.presentation.console.contollers;

import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readInt;
import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readString;

import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import paivanov.bankingsystem.service.client.ClientService;

@Component
@Profile("console")
public class ClientMenuController {

    private final Scanner scanner;
    private final ClientService clientService;

    @Autowired
    public ClientMenuController(
        @Qualifier("scanner") Scanner scanner,
        ClientService clientService
    ) {
        this.scanner = scanner;
        this.clientService = clientService;
    }

    public void showMenu() {

        if (!login()) {
            return;
        }

        while (true) {
            System.out.println("1. Просмотреть счета\n2. Перевести деньги\n3. Выход");
            int choice = readInt(scanner, "Выберите действие: ");

            switch (choice) {
                case 1 -> showAccounts();
                case 2 -> transferMoney();
                case 3 -> {
                    return;
                }
            }
        }
    }

    private boolean login() {
        int clientId = readInt(scanner, "Введите id клиента: ");
        String password = readString(scanner, "Введите пароль: ");

        try {
            clientService.login(clientId, password);
            System.out.println("Вход выполнен успешно!");
        } catch (Exception e) {
            System.out.println("Ошибка при входе: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void showAccounts() {
        try {
            System.out.println(clientService.getAllAccounts());
        } catch (Exception e) {
            System.out.println("Ошибка при получении счетов: " + e.getMessage());
        }
    }

    private void transferMoney() {
        int fromAccountId = readInt(scanner, "Введите ID счета отправителя: ");
        int toAccountId = readInt(scanner, "Введите ID счета получателя: ");
        int amount = readInt(scanner, "Введите сумму: ");
        try {
            clientService.transfer(fromAccountId, toAccountId, amount);
            System.out.println("Перевод выполнен успешно!");
        } catch (Exception e) {
            System.out.println("Ошибка при переводе: " + e.getMessage());
        }
    }
}
