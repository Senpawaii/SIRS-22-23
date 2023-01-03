package pt.tecnico.sirsproject.security;

public class SensorKeyResponse {
    public String getSymmetricKey() {
        return symmetricKey;
    }

    String symmetricKey;

    public SensorKeyResponse(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }
}
