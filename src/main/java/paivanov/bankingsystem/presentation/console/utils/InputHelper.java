package paivanov.bankingsystem.presentation.console.utils;

import java.util.Arrays;
import paivanov.bankingsystem.model.Currency;
import java.util.Scanner;

public class InputHelper {

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Введите число!");
            }
        }
    }

    public static String readString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static Currency readCurrency(Scanner scanner, String prompt) {
        System.out.print(prompt);
        System.out.println(Arrays.toString(Currency.values()));
        while (true) {
            try {
                return Currency.valueOf(scanner.nextLine().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Введите одну из доступных валют!");
            }
        }
    }

}

