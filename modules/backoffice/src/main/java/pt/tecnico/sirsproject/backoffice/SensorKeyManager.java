package pt.tecnico.sirsproject.backoffice;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.net.ssl.TrustManager;

import org.apache.commons.text.StringEscapeUtils;

import org.bouncycastle.util.BigIntegers;

import com.google.gson.Gson;

import pt.tecnico.sirsproject.security.SendRequest;
import pt.tecnico.sirsproject.security.UpdateSensorsKeyRequest;
import pt.tecnico.sirsproject.security.UpdateSensorsKeyResponse;

public class SensorKeyManager {
    private BigInteger p_prime;
    private BigInteger g_root;
    private BigInteger a_secret;
    private BigInteger bigA;
    private BigInteger bigB;
    private final String sensors_ip;
    private final String sensors_port;
    private TrustManager[] trustManagers;

    public SensorKeyManager(String sensors_ip, String sensors_port, TrustManager[] trustManagers) {
        this.sensors_ip = sensors_ip;
        this.sensors_port = sensors_port;
        this.trustManagers = trustManagers;
        generateParameters();
    }

    private void generateParameters() {
        p_prime = new BigInteger("99194853094755497");
        g_root = new BigInteger("2");
        
        // Random rand = new Random();
        a_secret = BigIntegers.createRandomBigInteger(64, new SecureRandom());
        bigA = g_root.modPow(a_secret, p_prime);
    }

    public String createNewSensorKey() throws KeyManagementException, NoSuchAlgorithmException, IOException {

        BigInteger newKey = executeDiffieHellman();

        String finalSymmetricKey = newKey.toString();

        return Base64.getEncoder().encodeToString(finalSymmetricKey.getBytes());
    }

    private BigInteger executeDiffieHellman() throws NoSuchAlgorithmException, KeyManagementException, IOException {

        System.out.println("==> Starting execution of Diffie-Helman!!");
        generateParameters();

        UpdateSensorsKeyRequest request = new UpdateSensorsKeyRequest(p_prime.toString(), g_root.toString(), bigA.toString());

        Gson gson = new Gson();
        String json = gson.toJson(request);

        // call the method to send request to sensors
        String response_json = "";
        try {
            response_json = SendRequest.sendRequest(sensors_ip, sensors_port, json, "POST", "/updatekey", trustManagers);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            System.out.println("Error: Failed to establish new secret key.");
            throw e;
        }

        String response_clean = StringEscapeUtils.unescapeJava(response_json.replaceAll("^\"|\"$", ""));
        UpdateSensorsKeyResponse response = gson.fromJson(response_clean, UpdateSensorsKeyResponse.class);
        
        bigB = new BigInteger(response.getBigB());
        // double newSecret = ((Math.pow(bigB, a_secret.doubleValue())) % p_prime.doubleValue());
        BigInteger newSecret = bigB.modPow(a_secret, p_prime);
        
        return newSecret;
    }

}
