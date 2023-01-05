package pt.tecnico.sirsproject.backoffice;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.TrustManager;

import org.apache.commons.text.StringEscapeUtils;

import org.bouncycastle.util.BigIntegers;
import org.json.JSONObject;

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

    private byte[] officePubKeyEncoded;
    KeyAgreement officeKeyAgree;

    public SensorKeyManager(String sensors_ip, String sensors_port, TrustManager[] trustManagers) {
        this.sensors_ip = sensors_ip;
        this.sensors_port = sensors_port;
        this.trustManagers = trustManagers;
        generateParameters();
    }

    private boolean generateParameters() {
        // p_prime = new BigInteger("85053461164796801949539541639542805770666392330682673302530819774105141531698707146930307290253537320447270457");
        // g_root = new BigInteger("2");
        
        // // a_secret = BigIntegers.createRandomBigInteger(64, new SecureRandom());
        // a_secret = BigIntegers.createRandomPrime(2048, 0, new SecureRandom());
        // bigA = g_root.modPow(a_secret, p_prime);

        try {
            KeyPairGenerator officeKeyPairGen = KeyPairGenerator.getInstance("DH");
            officeKeyPairGen.initialize(2048);
            KeyPair officeKeyPair = officeKeyPairGen.genKeyPair();

            this.officeKeyAgree = KeyAgreement.getInstance("DH");
            officeKeyAgree.init(officeKeyPair.getPrivate());

            this.officePubKeyEncoded = officeKeyPair.getPublic().getEncoded();

            return true;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println("Error: unable to generate parameters for Diffie-Hellman.");
            return false;
        }
    }

    public String createNewSensorKey() throws KeyManagementException, NoSuchAlgorithmException, IOException {

        byte[] newKey_seed = executeDiffieHellman();
        if (newKey_seed == null) {
            throw new IOException();
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(newKey_seed, 0, 16, "AES");
        return Base64.getEncoder().encodeToString(secretKeySpec.getEncoded());
        // SecretKey newKey = createAESKey(newKey_seed);
        // return Base64.getEncoder().encodeToString(newKey.getEncoded());
        
        // String encoded_seed = Base64.getEncoder().encodeToString(newKey_seed.toByteArray());
        // System.out.println("==> New seed: " + encoded_seed);

        // SecretKey newKey = createAESKey(newKey_seed.toByteArray());
        // String finalSymmetricKey = newKey.toString();

        // return Base64.getEncoder().encodeToString(finalSymmetricKey.getBytes());
        // return Base64.getEncoder().encodeToString(newKey.getEncoded());
    }

    private byte[] executeDiffieHellman() throws NoSuchAlgorithmException, KeyManagementException, IOException {

        System.out.println("==> Starting execution of Diffie-Helman!!");
        generateParameters();

        // UpdateSensorsKeyRequest request = new UpdateSensorsKeyRequest(p_prime.toString(), g_root.toString(), bigA.toString());
        
        // Gson gson = new Gson();
        // String json = gson.toJson(request);
        
        JSONObject request = new JSONObject();
        request.put("pub_key", Base64.getEncoder().encodeToString(officePubKeyEncoded));
        String json = request.toString();

        // call the method to send request to sensors
        String response_json = "";
        try {
            response_json = SendRequest.sendRequest(sensors_ip, sensors_port, json, "POST", "/updatekey", trustManagers);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            System.out.println("Error: Failed to establish new secret key.");
            throw e;
        }

        if (response_json.equals("")) {
            return null;
        }

        try {
            JSONObject response = new JSONObject(response_json);
            byte[] sensorsPubKeyEnc = Base64.getDecoder().decode(response.getString("sensors_pub_key"));
            
            KeyFactory officeKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sensorsPubKeyEnc);
            PublicKey sensorsPubKey = officeKeyFac.generatePublic(x509KeySpec);
            officeKeyAgree.doPhase(sensorsPubKey, true);

            byte[] officeSharedSecret = officeKeyAgree.generateSecret();
            int officeLen = officeSharedSecret.length;

            System.out.println("==> Secret: " + toHexString(officeSharedSecret));

            return officeSharedSecret;
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalStateException e) {
            e.printStackTrace();
        }
        

        // String response_clean = StringEscapeUtils.unescapeJava(response_json.replaceAll("^\"|\"$", ""));
        // UpdateSensorsKeyResponse response = gson.fromJson(response_clean, UpdateSensorsKeyResponse.class);
        
        // bigB = new BigInteger(response.getBigB());
        // BigInteger newSecret = bigB.modPow(a_secret, p_prime);
        
        // return newSecret;
        return null;
    }

    private SecretKey createAESKey(byte[] seed) {
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.setSeed(seed);

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

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

}
