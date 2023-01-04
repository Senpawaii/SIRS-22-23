package pt.tecnico.sirsproject.sensors;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Base64.Decoder;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import pt.tecnico.sirsproject.security.*;

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
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                sx.sendResponseHeaders(405, -1);
                sx.getRequestBody().close();
            }

            UpdateSensorsKeyRequest req = null;
            try {
                req = RequestParsing.parseUpdateSensorsKeyRequestToJSON(sx);
            } catch (IOException e) {
                e.printStackTrace();
                sx.sendResponseHeaders(400, -1);
                sx.getResponseBody().close();
            }

            int p = Integer.parseInt(req.getP_prime());
            int g = Integer.parseInt(req.getG_root());
            double bigA = Double.parseDouble(req.getBigA());

            int b_secret = new Random().nextInt();
            double bigB = ((Math.pow(g, b_secret)) % p);
            String bigB_str = String.valueOf(bigB);

            double newSecret = ((Math.pow(bigA, b_secret)) % p);
            String newKey = Base64.getEncoder().encodeToString(String.valueOf(newSecret).getBytes());
            sensors.updateCurrentKey(newKey);

            UpdateSensorsKeyResponse response = new UpdateSensorsKeyResponse(bigB_str);
            Gson gson = new Gson();
            String response_json = gson.toJson(response);
            sendResponse(sx, 200, response_json);

            // String newKey = req.getNew_key();

            // UpdateSensorsKeyRequest dummy = new UpdateSensorsKeyRequest("aCmsgqY9ixy+0IQSpgBhW3ZA1cdVOGlUV2UahF6Wc1g=");
            // Gson gson = new Gson();
            // String json_dummy = gson.toJson(dummy);
            // // System.out.println(json_dummy);

            // String clean = StringEscapeUtils.unescapeJava(json_dummy.replaceAll("^\"|\"$", ""));
            // UpdateSensorsKeyRequest req = gson.fromJson(clean, UpdateSensorsKeyRequest.class);
            // String newKey = req.getNew_key();

            // if (newKey == null) {
            //     // bad message, send 400
            //     sx.sendResponseHeaders(400, -1);
            //     sx.getResponseBody().close();
            // } else {
            //     // received new key, update
            //     sensors.updateCurrentKey(newKey);
            //     sendResponse(sx, 200, sensors.getEncodedCurrentKey());
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
