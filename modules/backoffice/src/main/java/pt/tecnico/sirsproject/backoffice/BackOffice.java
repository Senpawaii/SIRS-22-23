package pt.tecnico.sirsproject.backoffice;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.bson.Document;
import pt.tecnico.sirsproject.security.RSAUtils;
import pt.tecnico.sirsproject.security.SensorKey;
import pt.tecnico.sirsproject.security.SymmetricKeyEncryption;
import pt.tecnico.sirsproject.security.TLS_SSL;

import javax.crypto.Cipher;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;

public class BackOffice {
    private KeyStore keystore;
    private Properties properties;
    private TrustManager[] trustManagers;
    private KeyManager[] keyManagers;
    private SensorKey sensorKey;
    private final SessionManager manager = new SessionManager();
    private SSLContext sslContext;


    public BackOffice(String keystorePath) {
        loadProperties();
        loadKeyStore(keystorePath);
        setTrustManagers();
        setKeyManagers();
        setSSLContext();
        createSensorKey();
        createDatabaseConnection();
    }

    private void setTrustManagers() {
        HashMap<String, String> certificate_paths = new HashMap<>();
        // Insert here all the necessary certificates for the Client
        certificate_paths.put("Client_certificate", "../../extra_files/backoffice/outside_certificates/ClientCertificate.pem");
        certificate_paths.put("Sensors_certificate", "../../extra_files/backoffice/outside_certificates/SensorsCertificate.pem");

        KeyStore keystoreCertificates = RSAUtils.loadKeyStoreCertificates(certificate_paths);
        this.trustManagers = RSAUtils.loadTrustManagers(keystoreCertificates);
    }

    private void setKeyManagers() {
        String keystore_password = properties.getProperty("keystore_pass");
        char[] pass = keystore_password.toCharArray();

        this.keyManagers = RSAUtils.loadKeyManagers(this.keystore, pass);
    }

    private void setSSLContext() {
        try {
            sslContext = TLS_SSL.createSSLContext(trustManagers, keyManagers);
        } catch(NoSuchAlgorithmException | KeyManagementException e) {
            System.out.println("Error: Couldn't create SSL context. " + e.getMessage());
            System.exit(1);
        }
    }

    private void createSensorKey() {
        this.sensorKey = new SensorKey("DUMMY");
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

    private void createDatabaseConnection() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://backoffice:backoffice@192.168.0.100:27017/Users?ssl=true&sslInvalidCertificatesAllowed=true");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSslSettings(builder -> builder.enabled(true).context(this.sslContext))
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("Users");
        MongoCollection<Document> collection = database.getCollection("user_pass");
        Document doc = collection.find().first();
        if (doc != null) {
            System.out.println(doc.toJson());
        } else {
            System.out.println("No matching documents found.");
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
                    SSLParameters sslparameters = sslcontext.getDefaultSSLParameters();
                    params.setSSLParameters(sslparameters);
                    System.out.println("Server connected on port " + port + ".");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    System.out.println("Failed to create server.");
                }
            }
        });
        return server;
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