package pt.tecnico.sirsproject.security;

public class SensorKey {
    String symmetricKey;

    public SensorKey(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }

    public String getSymmetricKey() {
        return symmetricKey;
    }
}
