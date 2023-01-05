package pt.tecnico.sirsproject.client;

import com.google.gson.Gson;
import pt.tecnico.sirsproject.security.*;

import javax.net.ssl.TrustManager;
import java.io.*;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

public class Client {
    private String username;
    private String password;
    private final Properties properties = new Properties();
    private PublicKey backoffice_publicK;
    private final String backoffice_address;
    private final String backoffice_port;
    private String token;
    private TrustManager[] trustManagers;
    private SensorKey sensorKey;

    public Client() {
        loadPropertiesFile();
        this.backoffice_address = properties.getProperty("backoffice_ip_address");
        this.backoffice_port = properties.getProperty("backoffice_port");
        loadPublicKeys();
        setTrustManagers();
    }

    private void loadPublicKeys() {
        File backoffice_publicFile = new File("../../extra_files/client/outside_publicKeys/BackofficePublicKey.pem");
        try {
            this.backoffice_publicK = RSAUtils.loadPublicKey(backoffice_publicFile);
            System.out.println("Server public key:" + Base64.getEncoder().encodeToString(this.backoffice_publicK.getEncoded()));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error: Failed to load backoffice public key. " + e.getMessage());
            System.exit(1);
        }
    }

    private void loadPropertiesFile() {
        try (InputStream input = new FileInputStream("../../extra_files/client/app.properties")) {
            this.properties.load(input);
        } catch (IOException ex) {
            System.out.println("Error: Couldn't load Properties file. " + ex.getMessage());
            System.exit(1);
        }
    }

    private void setTrustManagers() {
        HashMap<String, String> certificate_paths = new HashMap<>();
        // Insert here all the necessary certificates for the Client
        certificate_paths.put("BackOffice_certificate", "../../extra_files/client/outside_certificates/BackofficeCertificate.pem");
        certificate_paths.put("Sensors_certificate", "../../extra_files/client/outside_certificates/SensorsCertificate.pem");

        KeyStore keystoreCertificates = RSAUtils.loadKeyStoreCertificates(certificate_paths);
        this.trustManagers = RSAUtils.loadTrustManagers(keystoreCertificates);
    }

    public boolean verify_credentials() {
        String type = "verify_auth";

        Gson gson = new Gson();
        CredentialsRequest gson_object = new CredentialsRequest(type, username, password);

        String request = gson.toJson(gson_object);

        String response_json = ClientCommunications.connect_to_backoffice(request, "POST", "/auth",
                this.backoffice_address, this.backoffice_port, this.trustManagers);

        CredentialsResponse response = gson.fromJson(response_json, CredentialsResponse.class);
        if(response != null) {
            setToken(response.getToken());
            return true;
        }
        return false;
    }

    public void obtainSensorKey() throws Exception {
        String type = "verify_auth";

        Gson gson = new Gson();

        SensorKeyRequest gson_object = new SensorKeyRequest(type, this.username, this.token);
        String request = gson.toJson(gson_object);

        String response = ClientCommunications.connect_to_backoffice(request, "POST", "/sensors",
                this.backoffice_address, this.backoffice_port, this.trustManagers);

        SensorKeyResponse sensorResponse = gson.fromJson(response, SensorKeyResponse.class);

        String sensorKey = sensorResponse.getSymmetricKey();

        if(sensorKey.equals("Null")) { // TODO: Validate that this is how the server tells the client that the token is invalid!
            throw new Exception("Invalid token.");
        }
        this.sensorKey = new SensorKey(sensorKey);
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String _username) {
        this.username = _username;
    }

    private void setPassword(String _password) {
        this.password = _password;
    }

    public void setIdentity(String _username, String _password) {
        setUsername(_username);
        setPassword(_password);
    }

    private void setToken(String _token) {
        this.token = _token;
    }
}