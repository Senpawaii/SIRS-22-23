package pt.tecnico.sirsproject.frontoffice;

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

public class FrontOffice {
    private static KeyStore keystore;
    private Properties properties;
    private final String backoffice_address;
    private final String backoffice_port;
    private TrustManager[] trustManagers;
    private KeyManager[] keyManagers;
    private SSLContext sslContext;
    private MongoClient mongoClient; 


    public FrontOffice(String keystorePath) {
    	loadProperties();
        this.backoffice_address = properties.getProperty("backoffice_ip_address");
        this.backoffice_port = properties.getProperty("backoffice_port");
    	loadKeyStore(keystorePath);
    	setTrustManagers();
        setKeyManagers();
        setSSLContext();
        //createDatabaseConnection(); 
    }

    private void setTrustManagers() {
        HashMap<String, String> certificate_paths = new HashMap<>();
        // Insert here all the necessary certificates for the Client
        certificate_paths.put("Client_certificate", "../../extra_files/frontoffice/outside_certificates/ClientCertificate.pem");
        certificate_paths.put("Client_certificate", "../../extra_files/frontoffice/outside_certificates/BackofficeCertificate.pem");
        certificate_paths.put("Mongo_certificate", "../../extra_files/frontoffice/outside_certificates/MongoDBCertificate.pem");

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

    private void loadProperties() {
        // Load properties file
        properties = new Properties();
        try {
            properties.load(new FileInputStream("../../extra_files/frontoffice/config.properties")); // TODO: Find a more reliable way of using relative paths
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
                "mongodb://frontoffice:frontoffice@192.168.0.100:27017/Users?ssl=true"); //TODO: Place the username/password in a properties.file
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToSslSettings(builder -> builder.enabled(true).context(this.sslContext).invalidHostNameAllowed(true))
                .applyConnectionString(connectionString)
                .build();

        mongoClient = MongoClients.create(settings);
    }

    public HttpsServer createTLSServer(int port)
    	throws TLSServerException {

    	InetSocketAddress address = new InetSocketAddress(port);

    	// Initialize the server
    	HttpsServer server;
    	try {
    		server = HttpsServer.create(address, 0);
    	} catch (IOException e) {
    		throw new TLSServerException("Error: Couldn't create server on requested port " + port + ".", e);
    	}

    	// Set the SSL context for the frontoffice server
    	server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
    		public void configure(HttpsParameters params) {
    			try {
    				SSLContext sslcontext = SSLContext.getDefault();
    				SSLEngine engine = sslcontext.createSSLEngine();
    				params.setNeedClientAuth(false); // TODO change to 'true'
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

    public MongoClient getMongoClient() {
        return this.mongoClient;
    }

    public String getBackofficeAddr() {
        return this.backoffice_address;
    }

    public String getBackofficePort() {
        return this.backoffice_port;
    }

    public TrustManager[] getTrustManagers() {
        return this.trustManagers;
    }
}
