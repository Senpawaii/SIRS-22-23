package pt.tecnico.sirsproject.security;

public class UpdateSensorsKeyResponse {
    private String bigB;

    public UpdateSensorsKeyResponse(String bigB) {
        this.bigB = bigB;
    }

    public String getBigB() {
        return this.bigB;
    }

    public void setBigB(String B) {
        this.bigB = B;
    }
}
