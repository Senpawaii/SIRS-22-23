package pt.tecnico.sirsproject.client;

import pt.tecnico.sirsproject.security.SendRequest;
import pt.tecnico.sirsproject.security.SensorKeyRequest;
import pt.tecnico.sirsproject.security.SensorKeyResponse;
import pt.tecnico.sirsproject.security.SymmetricKeyEncryption;

import com.google.gson.Gson;

import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ClientComms {
    private final Properties prop = new Properties();

    public ClientComms() {
        loadPropertiesFile();
    }

    private void loadPropertiesFile() {
        try (InputStream input = new FileInputStream("../../extra_files/client/app.properties")) {
            // load a properties file
            this.prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String connect_to_backoffice(String json_req, String type_req, String handler) {
        String address = prop.getProperty("backoffice_ip_address");
        String port = prop.getProperty("backoffice_port");

        System.out.println("Contacting Back office on address " + address + " and port: " + port + "...");

        String encrypted_response = null;
        try {
            encrypted_response = sendRequest(address, port, json_req, type_req, handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(encrypted_response == null) {return null;}
        return encrypted_response;
    }

    private String sendRequest(String address, String port, String json_req, String type_req, String handler) throws IOException {
        File crtFile = new File("../../extra_files/backoffice/server.crt");

        String response = SendRequest.sendRequest(address, port, json_req, type_req, handler, crtFile);
        return response;
    }

    public Map<String, Object> verify_credentials(String username, String hashed_password) {
        File publicKeyFile = new File("../../extra_files/backoffice/public.key");

        String encodedSymmetricKey = new SymmetricKeyEncryption().getEncodedSymmetricKey();
        System.out.println("EncodedSymmetricKey:" + encodedSymmetricKey);
        PublicKey serverPublic = null;
        try {
            serverPublic = RSASecurity.loadPublicKey(publicKeyFile);
            System.out.println("Server public key:" + Base64.getEncoder().encodeToString(serverPublic.getEncoded()));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        String encryptedAESKey = RSASecurity.encryptSecretKey(encodedSymmetricKey, serverPublic);

        String type = "verify_auth";
        String encrypted_username = SymmetricKeyEncryption.encrypt(username, encodedSymmetricKey);
        String encrypted_hash_password = SymmetricKeyEncryption.encrypt(hashed_password, encodedSymmetricKey);
        ;

        Gson gson = new Gson();
        CredentialsReq req = new CredentialsReq(type, encrypted_username, encrypted_hash_password, encryptedAESKey);

        String json_req = gson.toJson(req);
        System.out.println(json_req);

        SessionToken sessionToken = gson.fromJson(connect_to_backoffice(json_req, "POST", "/auth"), SessionToken.class);
        String encrypted_session_token = null;
        try {
            encrypted_session_token = sessionToken.getEncryptedToken();
        } catch (NullPointerException e) {
            System.out.println("Error: NullPointerException: " + e.getMessage());
        }

        sessionToken.setToken(SymmetricKeyEncryption.decrypt(encrypted_session_token, encodedSymmetricKey));

        Map<String, Object> values = new HashMap<>();
        values.put("sessionToken", sessionToken);
        values.put("symmetricKey", encodedSymmetricKey);
        return values;
    }

    public String requestSensorKey(String username, SessionToken token) {
        Gson gson = new Gson();
        String type = "verify_auth";
        String session_token = token.getToken();

        SensorKeyRequest req = new SensorKeyRequest(type, username, session_token);
        String json_req = gson.toJson(req);

        SensorKeyResponse response = gson.fromJson(connect_to_backoffice(json_req, "POST", "/sensors"), SensorKeyResponse.class);

        String sensorKey = null;
        try {
            sensorKey = response.getSymmetricKey();
        } catch (NullPointerException e) {
            System.out.println("Error: NullPointerException: " + e.getMessage());
        }

        return sensorKey;
    }

}
