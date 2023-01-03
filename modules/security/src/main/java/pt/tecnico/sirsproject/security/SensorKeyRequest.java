package pt.tecnico.sirsproject.security;

public class SensorKeyRequest extends Request{
    private final String type;
    private final String session_token;
    private final String username;

    public String getType() {
        return type;
    }

    public String getSession_token() {
        return session_token;
    }

    public String getUsername() {
        return username;
    }

    public SensorKeyRequest(String type, String username, String session_token) {
        this.type = type;
        this.session_token = session_token;
        this.username = username;
    }

}
