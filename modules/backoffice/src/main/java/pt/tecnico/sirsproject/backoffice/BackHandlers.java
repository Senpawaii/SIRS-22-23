package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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
            JSONObject reqBody;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                return;
            }

            try {
                reqBody = getRequestsBody(sx);
            } catch(IOException exception) {
                System.out.println(exception.getMessage());
                return;
            }
            String username = reqBody.getString("username");
            String hash_password = reqBody.getString("hash_password");

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

        private JSONObject getRequestsBody(HttpsExchange exc) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            
            String line = r.readLine();
            while(line != null) {
                sb.append(line);
                line = r.readLine();
            }
            r.close();
            String requestBody = sb.toString().replaceAll("^\"|\"$", "").replaceAll("\\\\\"", "");
            System.out.println(requestBody);
            JSONObject json_obj;
            try {
                json_obj = new JSONObject(requestBody);
            } catch(JSONException exception) {
                System.out.println(exception.getMessage());
                return null;
            }
            return json_obj;
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
