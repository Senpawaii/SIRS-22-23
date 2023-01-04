package pt.tecnico.sirsproject.security;

public class CredentialsRequest {
    private final String type;
    private final String username;
    private final String password;

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public CredentialsRequest(String type, String _username, String _hash_password) {
        this.type = type;
        this.username = _username;
        this.password = _hash_password;
    }
}
