package pt.tecnico.sirsproject.frontoffice;

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


public class FrontHandlers {
    public static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange x) throws IOException {

            JSONArray content = new JSONArray();
            content.put("Hello there from the Front-office!");
            content.put("The server is up and running:)");

            String response = content.toString(1);  // the argument "1" formats each entry into a seperate line

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

            CredentialsRequest credentialsRequest;
            try {
                credentialsRequest = RequestParsing.parseCredentialsRequestToJSON(sx);
            } catch(IOException e) {
                System.out.println("Error: Couldn't parse Credentials Request to JSON format. " + e.getMessage());
                return;
            }

            // TODO: Add validation for .getString
            assert credentialsRequest != null;
            String username = credentialsRequest.getUsername();
            String password = credentialsRequest.getPassword();
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

    public static void sendResponse(HttpsExchange x, int statusCode, String responseBody) throws IOException {
        // TODO: Handle IOException
        x.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream out =x.getResponseBody();
        out.write(responseBody.getBytes());
        out.close();
    }

    /* Add the other possible handlers the frontoffice might have here */
}
