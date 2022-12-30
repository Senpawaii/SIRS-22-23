package pt.tecnico.sirsproject.client;

public class Client {
    private String username;
    private String hashed_password;

    public String getUsername() {
        return username;
    }

    public String getHashed_password() {
        return hashed_password;
    }

    public Client(String username, String hashed_password) {
        this.username = username;
        this.hashed_password = hashed_password;
    }
}