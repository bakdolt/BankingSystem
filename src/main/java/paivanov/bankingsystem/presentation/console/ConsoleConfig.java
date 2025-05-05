package paivanov.bankingsystem.presentation.console;

import java.util.Scanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("console")
public class ConsoleConfig {

    // Создание бина Scanner, новый экземпляр при каждом внедрении
    @Bean
    @Scope("prototype")
    public Scanner scanner() {
        return new Scanner(System.in);
    }


}