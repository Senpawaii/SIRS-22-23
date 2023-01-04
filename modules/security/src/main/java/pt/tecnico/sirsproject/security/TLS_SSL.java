package pt.tecnico.sirsproject.security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public final class TLS_SSL {
    public static SSLContext createSSLContext(TrustManager[] trustManagers, KeyManager[] keyManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: TLS is not available in this environment. " + e.getMessage());
            throw new NoSuchAlgorithmException();
        }

        try {
            sslContext.init(keyManager, trustManagers, null);
        } catch (KeyManagementException e) {
            System.out.println("Error: Couldn't initialize SSL context. " + e.getMessage());
            throw new KeyManagementException();
        }
        return sslContext;
    }
}
