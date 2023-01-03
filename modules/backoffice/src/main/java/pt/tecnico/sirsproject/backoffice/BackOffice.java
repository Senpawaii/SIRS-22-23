package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import pt.tecnico.sirsproject.security.SensorKey;
import pt.tecnico.sirsproject.security.SymmetricKeyEncryption;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Properties;

public class BackOffice {
    private static KeyStore keystore;
    private Properties properties;
    private KeyManagerFactory keyManager;
    private TrustManagerFactory trustManager;
    private SensorKey sensorKey;
    private SessionManager manager = new SessionManager();


    public BackOffice(String keystorePath) {
        loadProperties();
        loadKeyStore(keystorePath);
        initializeKeyAndTrustManager();
        createSensorKey();
    }

    private void createSensorKey() {
        SecretKey aeskey = new SymmetricKeyEncryption().createAESKey();
        this.sensorKey = new SensorKey(aeskey);
    }

    private void loadProperties() {
        // Load properties file
        properties = new Properties();
        try {
            properties.load(new FileInputStream("../../extra_files/backoffice/config.properties")); // TODO: Find a more reliable way of using relative paths
        } catch (IOException e) {
            System.out.println("Error reading properties file: " + e.getMessage());
            System.exit(-1);
        }
    }

    private void loadKeyStore(String keystore_path) {
        // Initialize the keystore
        String password = properties.getProperty("keystore_pass");
        char[] pass = password.toCharArray();
        try {
            keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(keystore_path), pass);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            System.out.println("Error: Couldn't load KeyStore. " + e.getMessage());
        }
    }

    HttpsServer createTLSServer(int port)
            throws TLSServerException {

        InetSocketAddress address = new InetSocketAddress(port);

        // Initialize the server
        HttpsServer server;
        try {
            server = HttpsServer.create(address, 0);
        } catch (IOException e) {
            throw new TLSServerException("Error: Couldn't create server on requested port " + port + ".", e);
        }

        // Initialize the SSL context
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: Couldn't find requested SSL Context algorithm.", e);
        }


        // Initialize the SSL context with the key and trust managers
        try {
            context.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            throw new TLSServerException("Error: Unable to initialize SSL context.", e);
        }

        // Set the SSL context for the backoffice server
        server.setHttpsConfigurator(new HttpsConfigurator(context) {
            public void configure(HttpsParameters params) {
                try {
                    SSLContext sslcontext = SSLContext.getDefault();
                    SSLEngine engine = sslcontext.createSSLEngine();
                    params.setNeedClientAuth(false); // TODO: This needs to be true!
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // set SSL parameters
                    SSLParameters sslparams = sslcontext.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                    System.out.println("Server connected on port " + port + ".");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    System.out.println("Failed to create server.");
                }
            }
        });
        return server;
    }

    private void initializeKeyAndTrustManager() {
        String password = properties.getProperty("keystore_pass");
        char[] pass = password.toCharArray();

        // Initialize the key and trust manager factories
        try {
            keyManager = KeyManagerFactory.getInstance("SunX509");
            trustManager = TrustManagerFactory.getInstance("SunX509");
            keyManager.init(keystore, pass);
            trustManager.init(keystore);
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            System.out.println("Error: Couldn't initialize key or trust manager factory " + e.getMessage());
        }
    }

    byte[] decryptWithRSA(byte[] request) {
        String password = properties.getProperty("keystore_pass");
        char[] pass = password.toCharArray();

        byte[] decryptedRequest = new byte[0];
        try {
            // Load Private Key
            PrivateKey privateKey = (PrivateKey) keystore.getKey("private_key", pass); // First arg is the alias
            System.out.println("Server private key: " + Base64.getEncoder().encodeToString(privateKey.getEncoded()));

            // Decrypt request data
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decryptedRequest = cipher.doFinal(request);
        } catch (Exception e) {
            // TODO: Handle Exception
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return decryptedRequest;
    }

    String decryptWithSymmetric(String encrypted_data, byte[] key) {
        return SymmetricKeyEncryption.decrypt(encrypted_data, Base64.getEncoder().encodeToString(key));
    }

    public SensorKey getSensorKey() {
        return this.sensorKey;
    }

    public SessionManager getManager() {
        return this.manager;
    }
}