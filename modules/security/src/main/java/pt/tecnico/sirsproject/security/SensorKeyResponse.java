package pt.tecnico.sirsproject.security;

public class SensorKeyResponse {
    String symmetricKey;

    String extra_message;

    public String getExtra_message() { return extra_message; }

    public String getSymmetricKey() {
        return symmetricKey;
    }

    public SensorKeyResponse(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }
}
