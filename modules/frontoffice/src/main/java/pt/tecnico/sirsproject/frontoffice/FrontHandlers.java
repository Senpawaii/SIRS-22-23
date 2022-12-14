package pt.tecnico.sirsproject.frontoffice;

import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;


public class FrontHandlers {
    public static class PingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange x) throws IOException {
            String response = "Hello there from the Front-office!";
            x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            x.sendResponseHeaders(200, response.getBytes().length);
            OutputStream out = x.getResponseBody();
            out.write(response.getBytes());
            out.close();
        }
    }

    /* Add the other possible handlers the frontoffice might have here */
}
