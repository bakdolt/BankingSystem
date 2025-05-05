package paivanov.bankingsystem.presentation.console;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import paivanov.bankingsystem.presentation.console.contollers.MainMenuController;
import paivanov.bankingsystem.service.client.CurrentClientManager;

@Component
@Profile("console")
public class ConsoleRunner implements CommandLineRunner {

    private final MainMenuController mainMenuController;
    private final CurrentClientManager currentClientManager;

    @Autowired
    public ConsoleRunner(MainMenuController mainMenuController, CurrentClientManager currentClientManager) {
        this.mainMenuController = mainMenuController;
        this.currentClientManager = currentClientManager;
    }

    @Override
    public void run(String[] args) {
        try {
            mainMenuController.run();
        } finally {
            currentClientManager.logout();
        }
    }
}