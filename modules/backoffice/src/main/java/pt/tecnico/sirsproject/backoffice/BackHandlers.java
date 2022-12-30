package pt.tecnico.sirsproject.backoffice;

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

import com.google.gson.Gson;



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
                auth_request = parseRequestToJSON(sx);
            } catch(IOException exception) {
                System.out.println(exception.getMessage());
                return;
            }

            // TODO: Add validation for .getString
            byte[] encrypted_shared_key_b64 = auth_request.getEncrypted_shared_key().getBytes();
            System.out.println("Encrypted shared key base 64: " + Base64.getDecoder().decode(encrypted_shared_key_b64).toString());
            byte[] encrypted_shared_key = Base64.getDecoder().decode(encrypted_shared_key_b64);
            byte[] shared_key_b64 = BackMain.backoffice.decryptWithRSA(encrypted_shared_key);
//            byte[] shared_key_no_64 = BackMain.backoffice.decryptWithRSA(auth_request.getEncrypted_shared_key_no_64().getBytes());
            byte[] shared_key = Base64.getDecoder().decode(shared_key_b64);

            String encrypted_username_b64 = auth_request.getEncrypted_username();
            byte[] encrypted_username = Base64.getDecoder().decode(encrypted_username_b64.getBytes());

            String encrypted_hash_password_b64 = auth_request.getEncrypted_hash_password();
            byte[] encrypted_hash_password = Base64.getDecoder().decode(encrypted_hash_password_b64.getBytes());

            String username = BackMain.backoffice.decryptWithSymmetric(encrypted_username, shared_key);
            String hash_password = BackMain.backoffice.decryptWithSymmetric(encrypted_hash_password, shared_key);

            // TODO: Sanitize Strings

            if(validate_credentials(username, hash_password)) {
                // If the client already has a valid token, delete the current and generate a new one.
                if(this.manager.hashActiveSession(username)){
                    this.manager.deleteSession(username);
                }

                String token = this.manager.createSession(username);

                JSONObject response = new JSONObject();
                response.put("token", token);

                sendResponse(sx, 200, response.toString());
            } else {
                sendResponse(sx, 401, "Invalid credentials.");
            }

        }

        private AuthRequest parseRequestToJSON(HttpsExchange exc) throws IOException {
            InputStreamReader isr = new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String requestBody = removeQuotesAndUnescape(br.readLine());

            Gson gson = new Gson();
            AuthRequest request = null;
            try {
                request = gson.fromJson(requestBody, AuthRequest.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            return request;
        }

        private String removeQuotesAndUnescape(String uncleanJson) {
            String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");

            return StringEscapeUtils.unescapeJava(noQuotes);
        }

        private static boolean validate_credentials(String username, String hash_password) {
            // TODO: contact DB and check credentials
            return true;
        }
    }

    /* Add the other possible handlers the FrontOffice might have here */


    public static void sendResponse(HttpsExchange x, int statusCode, String responseBody) throws IOException {
        // TODO: Handle IOException
        x.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream out =x.getResponseBody();
        out.write(responseBody.getBytes());
        out.close();
    }
}
