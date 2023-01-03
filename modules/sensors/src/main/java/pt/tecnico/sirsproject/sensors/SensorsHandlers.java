package pt.tecnico.sirsproject.sensors;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import org.apache.commons.text.StringEscapeUtils;
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
            // sx.sendResponseHeaders(200, response.getBytes().length);
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
            // String requestMethod = sx.getRequestMethod();
            // if (!requestMethod.equals("POST")) {
            //     //ERROR case
            //     sx.sendResponseHeaders(405, -1);
            //     sx.getRequestBody().close();
            // }

            // BufferedReader reader = new BufferedReader(new InputStreamReader(sx.getRequestBody(), StandardCharsets.UTF_8));
            // String requestString = reader.readLine();
            
            // JSONObject request = new JSONObject(requestString);
            
            // String newKey = sensors.decryptRSA(request.getString("newKey")); // will be replaced with rsa util class

            // Simulate receiving an encrypted key for testing
            // String newKey_enc = sensors.encryptRSA();
            // String newKey_dec = sensors.decryptRSA(newKey_enc);

            // Simulate receiving a key for testing
            String newKey = Base64.getEncoder().encodeToString(sensors.createAESKey().getEncoded());
            System.out.println("==> Received new key (encoded): " + newKey);

            if (newKey == null) {
                // bad message, send 400
                sx.sendResponseHeaders(400, -1);
                sx.getResponseBody().close();
            } else {
                // received new key, update
                sensors.updateCurrentKey(newKey);
                sendResponse(sx, 200, sensors.getEncodedCurrentKey());
            }

            // if (newKey_dec == null) {
            //     // something went wrong with the decryption, send error
            //     sx.sendResponseHeaders(500, -1);
            //     sx.getResponseBody().close();
            // }
            // else {
            //     // success
            //     sensors.updateCurrentKey(newKey_dec);
            //     // sx.sendResponseHeaders(200, -1);
            //     // sx.getResponseBody().close();
            //     sendResponse(sx, 200, newKey_dec);
            // }
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
