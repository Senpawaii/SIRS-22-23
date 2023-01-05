package pt.tecnico.sirsproject.security;

public class ClientSensorsResponse {
    private String content;

    public ClientSensorsResponse(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
