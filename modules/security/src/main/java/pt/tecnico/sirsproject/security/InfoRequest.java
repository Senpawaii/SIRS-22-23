package pt.tecnico.sirsproject.security;

public class InfoRequest extends Request{
    private final String session_token;
    private final String username;

    public InfoRequest(String username, String session_token) {
        this.session_token = session_token;
        this.username = username;
    }

    public String getSession_token() {
        return session_token;
    }

    public String getUsername() {
        return username;
    }

}
