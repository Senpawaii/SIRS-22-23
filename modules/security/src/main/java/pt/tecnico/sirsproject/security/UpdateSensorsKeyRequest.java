package pt.tecnico.sirsproject.security;

public class UpdateSensorsKeyRequest {
    private String new_key;

    public UpdateSensorsKeyRequest(String new_key) {
        this.new_key = new_key;
    }

    public String getNew_key() {
        return this.new_key;
    }

    public void setNew_key(String new_key) {
        this.new_key = new_key;
    }
}
