package pt.tecnico.sirsproject.client;

public class SessionToken {
    private final String token;

    public String getToken() {
        return token;
    }

    public SessionToken(String _token) {
        this.token = _token;
    }
}
