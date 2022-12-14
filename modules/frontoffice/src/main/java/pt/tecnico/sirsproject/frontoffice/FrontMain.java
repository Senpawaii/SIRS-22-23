package pt.tecnico.sirsproject.frontoffice;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;

public class FrontMain {
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

    public static void main(String[] args) throws IOException {

        int port = -1;
        
        if (args.length != 1) {
            System.out.println("ARGS: <port>");
            System.exit(-1);
        }

        try {
            port = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException e) {
            System.out.println("ARGS: <port>");
            System.exit(-1);
        }

        try {
            System.out.println("Starting server on port " + port + "...");

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new PingHandler());
            server.setExecutor(null);

            System.out.println("Server started on port " + port + "!");
            server.start();
        } catch (Exception e) {
            System.out.println("Failed to create HTTP server");
            e.printStackTrace();
        }
    }
}
