package pt.tecnico.sirsproject.security;

public class CredentialsResponse {
    private final String token;

    public CredentialsResponse(String _token) {
        this.token = _token;
    }

    public String getToken() {
        return token;
    }
}
