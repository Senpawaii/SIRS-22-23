package pt.tecnico.sirsproject.sensors;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

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
    private SecretKeySpec currentKey;

    private static KeyStore keystore;
    private Properties properties;
    private KeyManager[] keyManagers;
    private TrustManager[] trustManagers;

    private SSLContext sslContext;
    private MongoClient mongoClient;

    public Sensors(String keystorePath) {
        loadProperties();
        loadKeyStore(keystorePath);
        setKeyManagers();
        setTrustManagers();
        setSSLContext();
        // createDatabaseConnection();
    }

    public String getEncodedCurrentKey() {
        byte[] encodedKey = currentKey.getEncoded();
        return Base64.getEncoder().encodeToString(encodedKey);
    }

    public void updateCurrentKey(byte[] newKey_seed) {
        SecretKeySpec newKey = new SecretKeySpec(newKey_seed, 0, 16, "AES");
        this.currentKey = newKey;
        System.out.println("==> New key (b64): " + getEncodedCurrentKey());
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
        // certificate_paths.put("Mongo_certificate", "../../extra_files/sensors/outside_certificates/MongoDBCertificate.pem");

        KeyStore keystoreCertificates = RSAUtils.loadKeyStoreCertificates(certificate_paths);
        this.trustManagers = RSAUtils.loadTrustManagers(keystoreCertificates);
    }

    private void setKeyManagers() {
        String keystore_password = properties.getProperty("keystore_pass");
        char[] pass = keystore_password.toCharArray();

        this.keyManagers = RSAUtils.loadKeyManagers(keystore, pass);
    }

    private void setSSLContext() {
        try {
            sslContext = TLS_SSL.createSSLContext(trustManagers, keyManagers);
        } catch(NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println("Error: Couldn't create SSL context. " + e.getMessage());
            System.exit(1);
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

    private void createDatabaseConnection() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://<username>:<password>@<databaseIP>:<databasePort>/SensorsLogs?ssl=true"); //TODO: Place the username/password in a properties.file
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSslSettings(builder -> builder.enabled(true).context(this.sslContext).invalidHostNameAllowed(true))
                .applyConnectionString(connectionString)
                .build();

        mongoClient = MongoClients.create(settings);

    }

    public void logClientRequest(String username) {
        System.out.println("==> Logging client request (dummy)");
        // here you send the log to the database
    }

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public SecretKey createAESKey(byte[] seed) {
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed);

        System.out.println("==> Seed length (bytes): " + seed.length);

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
}
