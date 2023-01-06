package pt.tecnico.sirsproject.security;

public class UserAuthenticatedResponse {
    private Boolean authenticated;

    public UserAuthenticatedResponse(Boolean authenticated) {
        this.authenticated = authenticated;
    }

    public Boolean isAuthenticated() {
        return authenticated;
    }
}
