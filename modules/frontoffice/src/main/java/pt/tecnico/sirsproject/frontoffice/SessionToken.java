package pt.tecnico.sirsproject.frontoffice;

import java.time.Instant;
import java.time.Duration;
import java.security.SecureRandom;

public class SessionToken {
    private static final int LENGTH = 128;
    private StringBuilder token = new StringBuilder(LENGTH);
    private final Instant deadline;

    private static final SecureRandom rand = new SecureRandom();
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public SessionToken() {
        // Generate 128 long session token
        for(int i = 0; i < LENGTH; i++) {
            token.append(CHARS.charAt(rand.nextInt(CHARS.length())));
        }
        this.deadline = Instant.now().plus(Duration.ofMinutes(2));
    }

    public Instant getDeadline() {
        return deadline;
    }

    public String getToken() {
        return this.token.toString();
    }
}
