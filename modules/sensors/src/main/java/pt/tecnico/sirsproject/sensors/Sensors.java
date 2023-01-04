package pt.tecnico.sirsproject.sensors;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import java.net.InetSocketAddress;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

import java.util.Properties;
import java.util.Base64;

import javax.net.ssl.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;


public class Sensors {
    private SecretKey currentKey;

    private static KeyStore keystore;
    private Properties properties;
    private KeyManagerFactory keyManager;
    private TrustManagerFactory trustManager;

//    private RSAService rsaService;

    public Sensors(String keystorePath) {
        loadProperties();
        loadKeyStore(keystorePath);
        initializeKeyAndTrustManager();

        // rsaService = new RSAService();

        currentKey = createAESKey(); //temporary, for testing
    }

    public String getEncodedCurrentKey() {
        byte[] encodedKey = currentKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public void updateCurrentKey(String newKey) {
        byte[] decoded_key = Base64.getDecoder().decode(newKey);
        currentKey = new SecretKeySpec(decoded_key, 0, decoded_key.length, "AES");
    }

    private void loadProperties() {
        // Load properties file
        properties = new Properties();
        try {
            properties.load(new FileInputStream("../../extra_files/sensors/config.properties")); // TODO: Find a more reliable way of using relative paths
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

    public SecretKey createAESKey() {
        SecureRandom secureRandom = new SecureRandom();

        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyGenerator != null;
        keyGenerator.init(256, secureRandom);
        return keyGenerator.generateKey();
    }

    // public String encryptRSA() {
    //     SecretKey secret = createAESKey();
    //     String encodedSecret = secret.getEncoded().toString();
    //     System.out.println("==> Encoded key to encrypt: " + encodedSecret);

    //     String encryptedData = null;
    //     try {
    //         KeyFactory factory = KeyFactory.getInstance("RSA");

    //         PemReader reader = new PemReader(new FileReader(new File("../../extra_files/sensors/public.key")));
    
    //         PemObject pemObject = reader.readPemObject();
    //         byte[] content = pemObject.getContent();
    //         X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
    
    //         PublicKey pubKey = factory.generatePublic(pubKeySpec);

    //         encryptedData = rsaService.encrypt(pubKey, secret.getEncoded());
    //         System.out.println("==> Encrypted and encoded key: " + encryptedData);
    //         System.out.println("==> Encoded length: " + encryptedData.length());
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    //     return encryptedData;
    // }

    // public String decryptRSA(String data) {
    //     String password = properties.getProperty("keystore_pass");
    //     char[] pass = password.toCharArray();

    //     System.out.println("==> Received encrypted and encoded key: " + data);
    //     System.out.println("==> Received length: " + data.length());

    //     String decryptedData = null;

    //     try {
    //         // Load Private Key
    //         PrivateKey privateKey = (PrivateKey) keystore.getKey("private_key", pass);

    //         // Decrypt data
    //         decryptedData = rsaService.decrypt(privateKey, data);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }

    //     return decryptedData;
    // }
}
