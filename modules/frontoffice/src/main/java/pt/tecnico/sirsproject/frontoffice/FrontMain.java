package pt.tecnico.sirsproject.frontoffice;

import com.sun.net.httpserver.HttpServer;

import pt.tecnico.sirsproject.frontoffice.FrontHandlers.PingHandler;

import java.io.*;
import java.net.InetSocketAddress;

public class FrontMain {
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
