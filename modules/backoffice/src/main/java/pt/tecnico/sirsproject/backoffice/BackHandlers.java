package pt.tecnico.sirsproject.backoffice;

import com.mongodb.client.MongoClient;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import org.json.JSONArray;
import org.json.JSONObject;

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
        private final MongoClient mongoClient;

        public AuthenticateHandler(SessionManager manager, MongoClient mongoClient) {
            this.manager = manager;
            this.mongoClient = mongoClient;
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

            CredentialsRequest credentials_request;
            try {
                credentials_request = RequestParsing.parseCredentialsRequestToJSON(sx);
            } catch(IOException e) {
                System.out.println("Error: Couldn't parse Credentials Request to JSON format. " + e.getMessage());
                return;
            }

            // TODO: Add validation for .getString
            assert credentials_request != null;
            String username = credentials_request.getUsername();
            String password = credentials_request.getPassword();
            // TODO: Sanitize Strings

            JSONObject response = new JSONObject();
            if(DatabaseCommunications.validate_credentials(username, password, this.mongoClient)) {

                // If the client already has a valid token, delete the current and generate a new one.
                if(this.manager.hashActiveSession(username)){
                    System.out.println("User: " + username + " already has an active session.");
                    this.manager.deleteSession(username);
                }
                String token = this.manager.createSession(username);

                response.put("token", token);
                System.out.println("Auth Request:" + username + " token: " + token);
                sendResponse(sx, 200, response.toString());
            } else {
                response.put("token", "Null");
                sendResponse(sx, 200, response.toString());
            }
        }
    }

    /* Add the other possible handlers the BackOffice might have here */
    public static class SensorKeyHandler implements HttpHandler {
        private final SensorKey sensorKey;
        private final SessionManager manager;
        private final AccessControlManager accessControlManager;
        private final Integer SENSOR_CLEARANCE = 2;

        public SensorKeyHandler(SensorKey key, SessionManager manager, AccessControlManager accessControlManager) {
            this.sensorKey = key;
            this.manager = manager;
            this.accessControlManager = accessControlManager;
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
            assert sensorKey_request != null;
            String sessionToken = sensorKey_request.getSession_token();
            String username = sensorKey_request.getUsername();

            JSONObject response = new JSONObject();
            if(validate_session(username, sessionToken, manager)) {
                if(accessControlManager.hasAccess(username, SENSOR_CLEARANCE)) {
                    response.put("symmetricKey", this.sensorKey.getSymmetricKey());
                    System.out.println("SensorKey Request:" + username + " sensorKey: " + sensorKey);
                    sendResponse(sx, 200, response.toString());
                } else {
                    response.put("symmetricKey", "None");
                    response.put("extra_message", "You do not have enough clearance to access this.");
                    sendResponse(sx, 200, response.toString());
                }
            } else {
                response.put("symmetricKey", "None");
                response.put("extra_message", "Invalid credentials.");
                sendResponse(sx, 200, response.toString());
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
        // Check if the SessionToken received matches the session token issued for this User, and has not expired yet.
        return token != null && sessionToken.equals(token.getToken()) && token.getDeadline().isAfter(Instant.now());
    }


}
