package pt.tecnico.sirsproject.client;

public class SessionToken {
    private String encrypted_token;
    private String token;

    public String getToken() {
        return token;
    }

    public String getEncryptedToken() {
        return encrypted_token;
    }

    public SessionToken(String encrypted_token) {
        this.encrypted_token = encrypted_token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
