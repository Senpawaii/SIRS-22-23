package pt.tecnico.sirsproject.sensors;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Base64.Decoder;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
// import com.sun.crypto.provider.SunJCE;

import javax.crypto.spec.DHParameterSpec;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import pt.tecnico.sirsproject.security.*;

import org.apache.commons.text.StringEscapeUtils;
import org.bouncycastle.util.BigIntegers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SensorsHandlers {
    // all the handler classes go here
    public static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange x) throws IOException {
        
            JSONArray content = new JSONArray();
            content.put("Hello there from the Sensors!");
            content.put("The server is up and running :)");

            String response = content.toString(1);  // the argument "1" formats each entry into a separate line

            HttpsExchange sx = (HttpsExchange) x;

            sx.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            sx.getResponseHeaders().set("Content-type", "application/json");
            sendResponse(sx, 200, response);
        }
    }

    public static class GetKeyHandler implements HttpHandler {
        private Sensors sensors;

        public GetKeyHandler(Sensors sensors) {
            this.sensors = sensors;
        }

        @Override
        public void handle(HttpExchange x) throws IOException {
            String encodedKey = sensors.getEncodedCurrentKey();

            HttpsExchange sx = (HttpsExchange) x;

            JSONObject content = new JSONObject();
            content.put("currentKey", encodedKey);

            String response = content.toString(1);
            sendResponse(sx, 200, response);
        }
    }

    public static class UpdateKeyHandler implements HttpHandler {
        private Sensors sensors;

        public UpdateKeyHandler(Sensors sensors) {
            this.sensors = sensors;
        }

        @Override
        public void handle(HttpExchange x) throws IOException {

            HttpsExchange sx = (HttpsExchange) x;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                sx.sendResponseHeaders(405, -1);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(sx.getRequestBody(), StandardCharsets.UTF_8));
            String requestBody = removeQuotesAndUnescape(reader.readLine());
            JSONObject req_json = new JSONObject(requestBody);
            byte[] officePubKeyEncoded = Base64.getDecoder().decode(req_json.getString("pub_key"));

            try {
                KeyFactory sensorsKeyFac = KeyFactory.getInstance("DH");
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(officePubKeyEncoded);
                
                PublicKey officePubKey = sensorsKeyFac.generatePublic(x509KeySpec);

                DHParameterSpec dhParamFromOfficePubKey = ((DHPublicKey)officePubKey).getParams();

                KeyPairGenerator sensorsKpairGen = KeyPairGenerator.getInstance("DH");
                sensorsKpairGen.initialize(dhParamFromOfficePubKey);
                KeyPair sensorsKpair = sensorsKpairGen.generateKeyPair();

                KeyAgreement sensorsKeyAgree = KeyAgreement.getInstance("DH");
                sensorsKeyAgree.init(sensorsKpair.getPrivate());

                byte[] sensorsPubKeyEnc = sensorsKpair.getPublic().getEncoded();
                String sensorsPubKeyEnc_b64 = Base64.getEncoder().encodeToString(sensorsPubKeyEnc);

                JSONObject response = new JSONObject();
                response.put("sensors_pub_key", sensorsPubKeyEnc_b64);
                String res_json = response.toString();
                sendResponse(sx, 200, res_json);

                sensorsKeyAgree.doPhase(officePubKey, true);

                byte[] sensorsSharedSecret = sensorsKeyAgree.generateSecret();
                int sensorsLen = sensorsSharedSecret.length;

                System.out.println("==> Secret: " + toHexString(sensorsSharedSecret));

                sensors.updateCurrentKey(sensorsSharedSecret);
                sx.close();
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                e.printStackTrace();
            }

            // UpdateSensorsKeyRequest req = null;
            // try {
            //     req = RequestParsing.parseUpdateSensorsKeyRequestToJSON(sx);

            //     if (req.getP_prime() == null || req.getG_root() == null || req.getBigA() == null) {
            //         sx.sendResponseHeaders(400, -1);
            //         return;
            //     }
            // } catch (IOException e) {
            //     e.printStackTrace();
            //     sx.sendResponseHeaders(400, -1);
            //     return;
            // }

            // BigInteger p = new BigInteger(req.getP_prime());
            // BigInteger g = new BigInteger(req.getG_root());
            // BigInteger bigA = new BigInteger(req.getBigA());

            // // BigInteger b_secret = BigIntegers.createRandomInRange(new BigInteger("100000000"), new BigInteger("1000000000000"), new SecureRandom());
            // BigInteger b_secret = BigIntegers.createRandomPrime(2048, 0, new SecureRandom());
            // BigInteger bigB = g.modPow(b_secret, p);
            // String bigB_str = bigB.toString();

            // BigInteger newSecret = bigA.modPow(b_secret, p);
            // String newKey_seed = Base64.getEncoder().encodeToString(newSecret.toByteArray());
            // sensors.updateCurrentKey(newKey_seed);

            // UpdateSensorsKeyResponse response = new UpdateSensorsKeyResponse(bigB_str);
            // Gson gson = new Gson();
            // String response_json = gson.toJson(response);
            // sendResponse(sx, 200, response_json);
        }
        static String removeQuotesAndUnescape(String uncleanJson) {
            String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");
    
            return StringEscapeUtils.unescapeJava(noQuotes);
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

    public static class ClientCommHandler implements HttpHandler {
        private Sensors sensors;

        public ClientCommHandler(Sensors sensors) {
            this.sensors = sensors;
        }

        @Override
        public void handle(HttpExchange x) throws IOException {
            String currentKey = sensors.getEncodedCurrentKey();

            HttpsExchange sx = (HttpsExchange) x;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("GET")) {
                // wrong method
                sx.sendResponseHeaders(405, -1);
                return;
            }

            ClientSensorsRequest req = null;
            try {
                req = RequestParsing.parseClientSensorsRequestToJSON(sx, currentKey);
            } catch (IOException | SensorsDecryptionException e) {
                // bad request
                sx.sendResponseHeaders(400, -1);
                sx.getResponseBody().close();
            }

            String username = req.getUsername();

            JSONObject response = new JSONObject();
            response.put("content", String.format("Hello from the Sensors/Actuators, %s.", username));
            
            // ClientSensorsResponse response = new ClientSensorsResponse(String.format("Hello from the Sensors/Actuators, %s.", username));
            // Gson gson = new Gson();
            String response_json = response.toString();
            sendResponse(sx, 200, response_json);

            sensors.logClientRequest(username);
        }
        
    }

    public static void sendResponse(HttpsExchange x, int statusCode, String responseBody) throws IOException {
        // TODO: Handle IOException
        x.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream out = x.getResponseBody();
        out.write(responseBody.getBytes());
        out.close();
    }
}
