package pt.tecnico.sirsproject.security;

public class ClientSensorsResponse {
    private String content;
    private String iv;

    public ClientSensorsResponse(String content, String iv) {
        this.content = content;
        this.iv = iv;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIv() {
        return this.iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
