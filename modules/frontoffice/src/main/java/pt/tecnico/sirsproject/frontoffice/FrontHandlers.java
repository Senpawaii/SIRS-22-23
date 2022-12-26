package pt.tecnico.sirsproject.frontoffice;

import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpsExchange;

// import org.json.JSONObject;
import org.json.JSONArray;
// import org.json.JSONException;


public class FrontHandlers {
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

    /* Add the other possible handlers the frontoffice might have here */
}
