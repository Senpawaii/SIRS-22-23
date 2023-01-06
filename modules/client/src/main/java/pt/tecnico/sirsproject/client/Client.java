package pt.tecnico.sirsproject.client;

import com.google.gson.Gson;
import pt.tecnico.sirsproject.security.*;

import javax.crypto.spec.SecretKeySpec;
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
    private final String sensors_address;
    private final String sensors_port;
    private final String frontoffice_address;
    private final String frontoffice_port;
    private String token;
    private TrustManager[] trustManagers;
    private SensorKey sensorKey = null;

    public Client() {
        loadPropertiesFile();
        this.backoffice_address = properties.getProperty("backoffice_ip_address");
        this.backoffice_port = properties.getProperty("backoffice_port");
        this.sensors_address = properties.getProperty("sensors_ip_address");
        this.sensors_port = properties.getProperty("sensors_port");
        this.frontoffice_address = this.backoffice_address;
        this.frontoffice_port = properties.getProperty("frontoffice_port");
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
        certificate_paths.put("FrontOffice_certificate", "../../extra_files/client/outside_certificates/FrontofficeCertificate.pem");
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

        if (response_json.startsWith("Http Error")) {
            System.out.println(response_json);
            return false;
        }

        CredentialsResponse response = gson.fromJson(response_json, CredentialsResponse.class);
        if(response != null) {
            String token = response.getToken();
            if(!token.equals("Null")) {
                setToken(token);
            } else {
                return false;
            }
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

        if (response.startsWith("Http Error")) {
            System.out.println(response);
            return;
        }

        SensorKeyResponse sensorResponse = gson.fromJson(response, SensorKeyResponse.class);

        String sensorKey_b64 = sensorResponse.getSymmetricKey();

        if(sensorKey_b64.equals("None")) {
            String cause = sensorResponse.getExtra_message();
            throw new Exception(cause);
        }
        SecretKeySpec spec = new SecretKeySpec(Base64.getDecoder().decode(sensorKey_b64.getBytes()), 0, 16, "AES");
        this.sensorKey = new SensorKey(spec);
    }

    public void accessSensors() {

        // check if client currently has sensor key
        if (this.sensorKey == null) {
            System.out.println("Error: You currently do not have a sensor key. Please request one and try again.");
            return;
        }

        Gson gson = new Gson();

        Container<byte[]> _ivContainer = new Container<>();
        String encryptedUsername = SymmetricKeyEncryption.encrypt(this.username, 
                                        Base64.getEncoder().encodeToString(this.sensorKey.getSymmetricKey().getEncoded()), _ivContainer, true);

        String encryptedToken = SymmetricKeyEncryption.encrypt(this.token, 
                                    Base64.getEncoder().encodeToString(this.sensorKey.getSymmetricKey().getEncoded()), _ivContainer, false);

        String encodedIV = Base64.getEncoder().encodeToString(_ivContainer.item);

        ClientSensorsRequest request = new ClientSensorsRequest(encryptedUsername, encryptedToken, encodedIV);
        String json = gson.toJson(request);

        String response_json = ClientCommunications.connect_to_sensors(json, "GET", "/getinfo", 
                            this.sensors_address, this.sensors_port, trustManagers);
        
        if (response_json.startsWith("Http Error")) {
            System.out.println(response_json);
            return;
        }

        ClientSensorsResponse response = gson.fromJson(response_json, ClientSensorsResponse.class);
        String encryptedContent = response.getContent();
        byte[] responseIV = Base64.getDecoder().decode(response.getIv());

        String decryptedContent = SymmetricKeyEncryption.decrypt(encryptedContent, Base64.getEncoder().encodeToString(this.sensorKey.getSymmetricKey().getEncoded()), responseIV);
        
        if (decryptedContent != null) {
            System.out.println(decryptedContent);
        }
    }

    public void obtainPublicInfo() throws Exception {
        Gson gson = new Gson();

        InfoRequest gson_object = new InfoRequest(this.username, this.token);
        String request = gson.toJson(gson_object);

        String response = ClientCommunications.connect_to_frontoffice(request, "POST", "/public",
                this.frontoffice_address, this.frontoffice_port, this.trustManagers);

        if (response.startsWith("Http Error")) {
            System.out.println(response);
            return;
        }

        PublicInfoResponse infoResponse = gson.fromJson(response, PublicInfoResponse.class);

        String stats = infoResponse.getStats();
        String shifts = infoResponse.getShifts();

        if(stats.equals("None") || shifts.equals("None")) {
            String cause = infoResponse.getExtra_message();
            throw new Exception(cause);
        } else {
            System.out.println("Stats: " + stats + ", Shifts: " + shifts);
        }
    }

    public void obtainPrivateInfo() throws Exception {
        Gson gson = new Gson();

        InfoRequest gson_object = new InfoRequest(this.username, this.token);
        String request = gson.toJson(gson_object);

        String response = ClientCommunications.connect_to_frontoffice(request, "POST", "/private",
                this.frontoffice_address, this.frontoffice_port, this.trustManagers);

        PrivateInfoResponse infoResponse = gson.fromJson(response, PrivateInfoResponse.class);

        String salary = infoResponse.getSalary();
        String absentWorkingDays = infoResponse.getAbsentWorkingDays();
        String parentalLeaves = infoResponse.getParentalLeaves();

        if(salary.equals("None") || absentWorkingDays.equals("None") || 
            parentalLeaves.equals("None")) {
            String cause = infoResponse.getExtra_message();
            throw new Exception(cause);
        } else {
            System.out.println("Salary: " + salary + ", Absent Working Days: " + absentWorkingDays + 
            ", Parental Leaves: " + parentalLeaves);
        }
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