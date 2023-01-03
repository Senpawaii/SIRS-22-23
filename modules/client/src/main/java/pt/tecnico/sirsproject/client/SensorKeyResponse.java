package pt.tecnico.sirsproject.client;

public class SensorKeyResponse {
    public String getSymmetricKey() {
        return symmetricKey;
    }

    String symmetricKey;

    public SensorKeyResponse(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }
}
