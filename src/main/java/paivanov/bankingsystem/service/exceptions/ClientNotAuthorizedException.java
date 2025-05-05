package paivanov.bankingsystem.service.exceptions;

public class ClientNotAuthorizedException extends Exception {

    public ClientNotAuthorizedException(String message) {
        super(message);
    }
}
