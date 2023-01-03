package pt.tecnico.sirsproject.frontoffice;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final Map<String, SessionToken> sessions;

    public SessionManager() {
        this.sessions = new HashMap<>();
    }

    String createSession(String username) {
        // Create a new session object and add it to the map
        SessionToken token = new SessionToken();
        sessions.put(username, token);
        return token.getToken();
    }

    boolean hashActiveSession(String username) {
        // Check if the client has a session token.
        return (sessions.containsKey(username));
    }

    void deleteSession(String username) {
        sessions.remove(username);
    }
}
