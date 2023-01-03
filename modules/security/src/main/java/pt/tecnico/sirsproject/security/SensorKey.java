package pt.tecnico.sirsproject.security;

import javax.crypto.SecretKey;

public class SensorKey {
    SecretKey symmetricKey;

    public SensorKey(SecretKey symmetricKey) {
        this.symmetricKey = symmetricKey;
    }
}
