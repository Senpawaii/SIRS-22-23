package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

import org.json.JSONArray;
// import org.json.JSONException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPointer;


public class BackHandlers {
    public static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange x) throws IOException {

            JSONArray content = new JSONArray();
            content.put("Hello there from the Front-office!");
            content.put("The server is up and running :)");

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
        @Override
        public void handle(HttpExchange x) throws IOException {
            // int maxRequestSize = 1024; // Check if the client is not sending a request too large!
            // x.setFixedLenghtStreamingMode(maxRequestSize); TODO: Set maximum size for requests.

            HttpsExchange sx = (HttpsExchange) x;
            JSONObject reqBody;
            String requestMethod = sx.getRequestMethod();
            if (!requestMethod.equals("POST")) {
                //ERROR case
                return;
            }

            try {
                reqBody = getRequestsBody(sx);
            } catch(IOException ioexc) {
                System.out.println(ioexc);
                return;
            }
            String username = reqBody.getString("username");
            String hash_password = reqBody.getString("hash_password");

            // TODO: Sanitize Strings

            if(validate_credentials(username, hash_password)) {
                String token = generateSecureToken(username);

                JSONObject response = new JSONObject();
                response.put("token", token);

                sendResponse(sx, 200, response.toString());
            } else {
                sendResponse(sx, 401, "Invalid credentials.");
            }

        }

        private static JSONObject getRequestsBody(HttpsExchange exc) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(exc.getRequestBody(), "utf-8"));
            StringBuilder sb = new StringBuilder();
            
            String line = r.readLine();
            while(line != null) {
                sb.append(line);
                line = r.readLine();
            }
            r.close();
            String requestBody = sb.toString().replaceAll("^\"|\"$", "").replaceAll("\\\\\"", "");;
            System.out.println(requestBody);
            JSONObject jobj;
            try {
                jobj = new JSONObject(requestBody);
            } catch(JSONException jexc) {
                System.out.println(jexc);
                return null;
            }
            return jobj;
        }

        private static String generateSecureToken(String username) { 
            // TODO: Generate secure session token for User username and store it: DB or in-memory cache
            return "ThisIsTotallySafe";
        } 

        private static boolean validate_credentials(String username, String hash_password) {
            // TODO: contact DB and check credentials
            return true;
        }
    }

    /* Add the other possible handlers the frontoffice might have here */


    public static void sendResponse(HttpsExchange x, int statusCode, String responseBody) throws IOException {
        // TODO: Handle IOException
        x.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream out =x.getResponseBody();
        out.write(responseBody.getBytes());
        out.close();
    }
}
