package paivanov.bankingsystem.service.utils;

public class PasswordHasher {

    private static final long A = 37L;

    public static Long getHash(String password) {
        long hash = 0L;
        for (int i = 0; i < password.length(); i++) {
            hash = hash * A + password.charAt(i);
        }
        return hash;
    }
}
