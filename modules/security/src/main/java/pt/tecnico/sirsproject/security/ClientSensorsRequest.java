package pt.tecnico.sirsproject.security;

public class ClientSensorsRequest extends Request {
    private String username;
    private String token;
    private String iv;

    public ClientSensorsRequest(String username, String token, String iv) {
        this.username = username;
        this.token = token;
        this.iv = iv;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIv() {
        return this.iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
