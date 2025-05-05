package paivanov.bankingsystem.presentation.console.contollers;

import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readInt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Profile("console")
public class MainMenuController {

    private final Scanner scanner;
    private final ClientMenuController clientMenuController;
    private final AdminMenuController adminMenuController;

    @Autowired
    public MainMenuController(
        @Qualifier("scanner") Scanner scanner,
        ClientMenuController clientMenuController,
        AdminMenuController adminMenuController
    ) {
        this.scanner = scanner;
        this.clientMenuController = clientMenuController;
        this.adminMenuController = adminMenuController;
    }

    public void run() {
        while (true) {
            System.out.println("1. Войти как администратор\n2. Войти как клиент\n3. Выход");
            int choice = readInt(scanner, "Выберите действие: ");

            switch (choice) {
                case 1 -> adminMenuController.showMenu();
                case 2 -> clientMenuController.showMenu();
                case 3 -> {
                    return;
                }
            }
        }
    }
} 