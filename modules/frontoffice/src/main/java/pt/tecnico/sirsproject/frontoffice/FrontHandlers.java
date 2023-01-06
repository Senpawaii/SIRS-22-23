package pt.tecnico.sirsproject.frontoffice;

import com.mongodb.client.MongoClient;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.TrustManager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import com.google.gson.Gson;
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

    public static class PublicInfoHandler implements HttpHandler {
        private final String backoffice_address;
        private final String backoffice_port;
        private final TrustManager[] trustManagers;

        public PublicInfoHandler(String backoffice_address, String backoffice_port, TrustManager[] trustManagers) {
            this.backoffice_address = backoffice_address;
            this.backoffice_port = backoffice_port;
            this.trustManagers = trustManagers;
        }

        @Override
        public void handle(HttpExchange ex) throws IOException {
            HttpsExchange sx = (HttpsExchange) ex;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                sx.sendResponseHeaders(405, -1);
                return;
            }

            PublicInfoRequest publicInfoRequest;
            try {
                publicInfoRequest = RequestParsing.parsePublicInfoRequestToJSON(sx);
            } catch(IOException exception) {
                System.out.println(exception.getMessage());
                sx.sendResponseHeaders(400, -1);
                return;
            }

            // Verify session token
            assert publicInfoRequest != null;
            String sessionToken = publicInfoRequest.getSession_token();
            String username = publicInfoRequest.getUsername();

            JSONObject response = new JSONObject();
            if(validate_session(username, sessionToken, 
                backoffice_address, backoffice_port, trustManagers)) {
                response.put("stats", "These are the stats");
                response.put("shifts", "These are the shifts");
                System.out.println("PublicInfo Request: " + username);
                sendResponse(sx, 200, response.toString());
            } else {
                response.put("stats", "None");
                response.put("shifts", "None");
                response.put("extra_message", "Invalid credentials.");
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

    public static String connect_to_backoffice(String request, String requestType, String handler, String address,
                                               String port, TrustManager[] trustManagers) {
        System.out.println("Connecting to BackOffice on address " + address + " and port: " + port + "...");
        String result = "";
        try {
            result = SendRequest.sendRequest(address, port, request, requestType, handler, trustManagers);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            System.out.println("Error: Failed to connect to the BackOffice.");
            System.exit(1);
        }
        return result;
    }

    private static boolean validate_session(String username, String sessionToken, String backoffice_address, 
        String backoffice_port, TrustManager[] trustManagers) {
        Gson gson = new Gson();

        UserAuthenticatedRequest gson_object = new UserAuthenticatedRequest(username, sessionToken);
        String request = gson.toJson(gson_object);

        String response = connect_to_backoffice(request, "POST", "/frontAuthentication",
                backoffice_address, backoffice_port, trustManagers);

        if (response.startsWith("Http Error")) {
            System.out.println(response);
            return false;
        }

        UserAuthenticatedResponse authenticationResponse = gson.fromJson(response, UserAuthenticatedResponse.class);

        Boolean authenticated = authenticationResponse.isAuthenticated();

        return authenticated;
    }
    /* Add the other possible handlers the frontoffice might have here */
}