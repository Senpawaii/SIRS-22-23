package pt.tecnico.sirsproject.security;

import javax.crypto.spec.SecretKeySpec;

public class SensorKey {
    SecretKeySpec symmetricKey;

    public SensorKey(SecretKeySpec symmetricKey) {
        this.symmetricKey = symmetricKey;
    }

    public SecretKeySpec getSymmetricKey() {
        return symmetricKey;
    }
}
