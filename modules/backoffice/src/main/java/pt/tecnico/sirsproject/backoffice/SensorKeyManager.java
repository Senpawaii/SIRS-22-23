package pt.tecnico.sirsproject.backoffice;

import java.util.Base64;
import java.util.Random;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;

import pt.tecnico.sirsproject.security.RequestParsing;
import pt.tecnico.sirsproject.security.UpdateSensorsKeyRequest;
import pt.tecnico.sirsproject.security.UpdateSensorsKeyResponse;

public class SensorKeyManager {
    private int p_prime;
    private int g_root;
    private int a_secret;
    private double bigA;
    private double bigB;
    private final String sensors_ip;
    private final String sensors_port;

    public SensorKeyManager(String sensors_ip, String sensors_port) {
        this.sensors_ip = sensors_ip;
        this.sensors_port = sensors_port;
        generateParameters();
    }

    private void generateParameters() {
        p_prime = 433494437;
        g_root = 2;
        
        Random rand = new Random();
        a_secret = rand.nextInt();
        bigA = ((Math.pow(g_root, a_secret)) % p_prime);
    }

    public String createNewSensorKey() {
        generateParameters();

        double newKey = executeDiffieHellman();

        String finalSymmetricKey = String.valueOf(newKey);

        return Base64.getEncoder().encodeToString(finalSymmetricKey.getBytes());
    }

    private double executeDiffieHellman() {
        // UpdateSensorsKeyRequest request = new UpdateSensorsKeyRequest(String.valueOf(p_prime), String.valueOf(g_root), String.valueOf(bigA));

        // Gson gson = new Gson();
        // String json = gson.toJson(request);

        // call the method to send request to sensors
        // String response_json = ...

        // String response_clean = StringEscapeUtils.unescapeJava(response_json.replaceAll("^\"|\"$", ""));
        // UpdateSensorsKeyResponse response = gson.fromJson(response_clean, UpdateSensorsKeyResponse.class);
        
        // bigB = response.getBigB();
        // double newSecret = ((Math.pow(bigB, a_secret)) % p_prime);
        
        // return newSecret;

        return 18;
    }

}
