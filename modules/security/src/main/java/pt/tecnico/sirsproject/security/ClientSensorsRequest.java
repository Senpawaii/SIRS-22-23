package pt.tecnico.sirsproject.security;

public class ClientSensorsRequest extends Request {
    private String username;

    public ClientSensorsRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
