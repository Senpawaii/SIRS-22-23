package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import pt.tecnico.sirsproject.security.*;
import pt.tecnico.sirsproject.security.RequestParsing;


public class BackHandlers {
    public static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange x) throws IOException {

            JSONArray content = new JSONArray();
            content.put("Hello there from the Front-office!");
            content.put("The server is up and running :)");

            String response = content.toString(1);  // the argument "1" formats each entry into a separate line

            HttpsExchange sx = (HttpsExchange) x;

            sx.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            sx.getResponseHeaders().set("Content-type", "application/json");
            sx.sendResponseHeaders(200, response.getBytes().length);

            OutputStream out = sx.getResponseBody();
            out.write(response.getBytes());
            out.close();
        }
    }

    public static class AuthenticateHandler implements HttpHandler {
        private final SessionManager manager;

        public AuthenticateHandler(SessionManager manager) {
            this.manager = manager;
        }

        @Override
        public void handle(HttpExchange x) throws IOException {
            // int maxRequestSize = 1024; // Check if the client is not sending a request too large!
            // x.setFixedLengthStreamingMode(maxRequestSize); TODO: Set maximum size for requests.

            HttpsExchange sx = (HttpsExchange) x;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                return;
            }

            AuthRequest auth_request;
            try {
                auth_request = RequestParsing.parseAuthRequestToJSON(sx);
            } catch(IOException exception) {
                System.out.println(exception.getMessage());
                return;
            }

            // TODO: Add validation for .getString
            byte[] encrypted_shared_key_b64 = auth_request.getEncrypted_shared_key().getBytes();
            byte[] encrypted_shared_key = Base64.getDecoder().decode(encrypted_shared_key_b64);
            byte[] shared_key_b64 = BackMain.backoffice.decryptWithRSA(encrypted_shared_key);
            byte[] shared_key = Base64.getDecoder().decode(shared_key_b64);

            String encrypted_username_b64 = auth_request.getEncrypted_username();
            byte[] encrypted_username = Base64.getDecoder().decode(encrypted_username_b64.getBytes());

            String encrypted_hash_password_b64 = auth_request.getEncrypted_hash_password();
            byte[] encrypted_hash_password = Base64.getDecoder().decode(encrypted_hash_password_b64.getBytes());

            String username = BackMain.backoffice.decryptWithSymmetric(Base64.getEncoder().encodeToString(encrypted_username), shared_key);
            String hash_password = BackMain.backoffice.decryptWithSymmetric(Base64.getEncoder().encodeToString(encrypted_hash_password), shared_key);

            // TODO: Sanitize Strings

            if(validate_credentials(username, hash_password)) {
                // If the client already has a valid token, delete the current and generate a new one.
                if(this.manager.hashActiveSession(username)){
                    this.manager.deleteSession(username);
                }
                String token = this.manager.createSession(username);

                // Encrypt the token with the shared symmetric key
                String encrypted_token = SymmetricKeyEncryption.encrypt(token, Base64.getEncoder().encodeToString(shared_key));

                JSONObject response = new JSONObject();
                response.put("encrypted_token", encrypted_token);
                System.out.println("Auth Request:" + username + " token: " + token);
                sendResponse(sx, 200, response.toString());
            } else {
                sendResponse(sx, 401, "Invalid credentials.");
            }

        }

        private static boolean validate_credentials(String username, String hash_password) {
            // TODO: contact DB and check credentials
            return true;
        }
    }

    /* Add the other possible handlers the BackOffice might have here */
    public static class SensorKeyHandler implements HttpHandler {
        private final SensorKey sensorKey;
        private final SessionManager manager;
        public SensorKeyHandler(SensorKey key, SessionManager manager) {
            this.sensorKey = key;
            this.manager = manager;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            HttpsExchange sx = (HttpsExchange) ex;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                return;
            }

            SensorKeyRequest sensorKey_request;
            try {
                sensorKey_request = RequestParsing.parseSensorKeyRequestToJSON(sx);
            } catch(IOException exception) {
                System.out.println(exception.getMessage());
                return;
            }

            // Verify session token
            String sessionToken = sensorKey_request.getSession_token();
            String username = sensorKey_request.getUsername();

            if(validate_session(username, sessionToken, manager)) {
                JSONObject response = new JSONObject();
                response.put("symmetricKey", this.sensorKey);
                System.out.println("SensorKey Request:" + username + " sensorKey: " + sensorKey);
                sendResponse(sx, 200, response.toString());
            } else {
                sendResponse(sx, 401, "Invalid credentials.");
            }


                if(this.manager.hashActiveSession(username)){

                }
        }
    }

    public static void sendResponse(HttpsExchange x, int statusCode, String responseBody) throws IOException {
        // TODO: Handle IOException
        x.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream out =x.getResponseBody();
        out.write(responseBody.getBytes());
        out.close();
    }

    private static boolean validate_session(String username, String sessionToken, SessionManager manager) {
        SessionToken token = manager.getSession(username);
        if(token != null && sessionToken.equals(token.getToken()) && token.getDeadline().isAfter(Instant.now()))
           return true;
        return false;
    }
}
