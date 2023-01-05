package pt.tecnico.sirsproject.sensors;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import pt.tecnico.sirsproject.security.RSAUtils;
import pt.tecnico.sirsproject.security.TLS_SSL;

import java.net.InetSocketAddress;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.X509EncodedKeySpec;

import java.util.Properties;
import java.util.Base64;
import java.util.HashMap;

import javax.net.ssl.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;


public class Sensors {
    private SecretKey currentKey;

    private static KeyStore keystore;
    private Properties properties;
    private KeyManager[] keyManagers;
    private TrustManager[] trustManagers;

//    private RSAService rsaService;

    public Sensors(String keystorePath) {
        loadProperties();
        loadKeyStore(keystorePath);
        setKeyManagers();
        setTrustManagers();

        // rsaService = new RSAService();

        currentKey = createAESKey(); //temporary, for testing
    }

    public String getEncodedCurrentKey() {
        byte[] encodedKey = currentKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public void updateCurrentKey(String newKey) {
        System.out.println("==> New encoded key: " + newKey);
        System.out.println("==> Random encoded key: " + Base64.getEncoder().encodeToString(createAESKey().getEncoded()));
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

    private void setTrustManagers() {
        HashMap<String, String> certificate_paths = new HashMap<>();
        // Insert here all the necessary certificates for the Client
        certificate_paths.put("Backoffice_certificate", "../../extra_files/sensors/outside_certificates/BackofficeCertificate.pem");
        certificate_paths.put("Client_certificate", "../../extra_files/sensors/outside_certificates/ClientCertificate.pem");

        KeyStore keystoreCertificates = RSAUtils.loadKeyStoreCertificates(certificate_paths);
        this.trustManagers = RSAUtils.loadTrustManagers(keystoreCertificates);
    }

    private void setKeyManagers() {
        String keystore_password = properties.getProperty("keystore_pass");
        char[] pass = keystore_password.toCharArray();

        this.keyManagers = RSAUtils.loadKeyManagers(keystore, pass);
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
        SSLContext sslContext = null;
        try {
            sslContext = TLS_SSL.createSSLContext(trustManagers, keyManagers);
        } catch(NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println("Error: Couldn't create SSL context. " + e.getMessage());
            System.exit(1);
        }

        // Set the SSL context for the backoffice server
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
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
