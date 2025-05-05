package paivanov.bankingsystem.presentation.console.contollers;

import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readCurrency;
import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readInt;
import static paivanov.bankingsystem.presentation.console.utils.InputHelper.readString;

import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import paivanov.bankingsystem.dto.AccountDto;
import paivanov.bankingsystem.dto.ClientDto;
import paivanov.bankingsystem.model.Currency;
import paivanov.bankingsystem.service.admin.AdminService;

@Component
@Profile("console")
public class AdminMenuController {

    private final Scanner scanner;
    private final AdminService adminService;

    @Autowired
    public AdminMenuController(@Qualifier("scanner") Scanner scanner, AdminService adminService) {
        this.scanner = scanner;
        this.adminService = adminService;
    }

    public void showMenu() {
        while (true) {
            System.out.println("1. Создать клиента\n2. Создать счет\n3. Выход");
            int choice = readInt(scanner, "Выберите действие: ");

            switch (choice) {
                case 1 -> createClient();
                case 2 -> createAccount();
                case 3 -> {
                    return;
                }
            }
        }
    }

    private void createClient() {
        String name = readString(scanner, "Введите имя клиента: ");
        String password = readString(scanner, "Введите пароль: ");
        ClientDto client = adminService.createClient(name, password);
        System.out.println("Клиент создан успешно:\n" + client);
    }

    private void createAccount() {
        Currency currency = readCurrency(scanner, "Введите валюту:\n");
        int balance = readInt(scanner, "Введите баланс: ");
        int clientId = readInt(scanner, "Введите id клиента: ");
        try {
            AccountDto account = adminService.createAccount(currency, balance, clientId);
            System.out.println("Счет создан успешно:\n" + account);
        } catch (Exception e) {
            System.out.println("Ошибка при создании счета: " + e.getMessage());
        }
    }
}
