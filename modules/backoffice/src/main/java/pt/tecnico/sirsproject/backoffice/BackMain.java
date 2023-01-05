package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpsServer;
import pt.tecnico.sirsproject.backoffice.BackHandlers.AuthenticateHandler;
import pt.tecnico.sirsproject.backoffice.BackHandlers.PingHandler;

import java.io.File;

public class BackMain {
    static BackOffice backoffice;

    public static void main(String[] args) {
//        GenerateJKS jks = new GenerateJKS(); // Generate JKS keystore

        int port = -1;
        String ksFile = "";

        if (args.length != 2) {
             System.out.println("ARGS: <port> <keyStorePath>");
             System.exit(-1);
        }

        try {
            port = Integer.parseInt(args[0]);
            ksFile = args[1];
        } catch (IllegalArgumentException e) {
            System.out.println("ARGS: <port> <keystorePassword>");
            System.exit(-1);
        }

        validateArgs(port, ksFile);

        backoffice = new BackOffice(ksFile);

        // Start HTTPS server
        try {
            System.out.println("Starting server on port " + port + "...");
            HttpsServer server = backoffice.createTLSServer(port);
            server.createContext("/test", new PingHandler());
            server.createContext("/auth", new AuthenticateHandler(backoffice.getManager(), backoffice.getMongoClient()));
            server.createContext("/sensors", new BackHandlers.SensorKeyHandler(/*backoffice.getSensorKey(), backoffice.getManager()*/backoffice));
            server.setExecutor(null);
            System.out.println("Server started on port " + port + "!");
            server.start();
        } catch (TLSServerException e) {
            System.out.println("Failed to create HTTPS server!!");
            System.out.println("==> " + e.getMessage());
            System.out.println("Cause of the error:");
            System.out.println("==> " + e.getCause().getMessage());
            e.printStackTrace();
        }
    }

    private static void validateArgs(int port, String ksFile) {
        if(port < 4000 || port > 65535) {
            System.out.println("Error: Invalid port number. Port numbers should be between 0 and 65535");
            System.exit(1);
        }

        try {
            File ksFileObj = new File(ksFile);
            if(!ksFileObj.exists() || !ksFileObj.canRead()) {
                System.out.println("Error: Invalid keystore file.");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Error: Failed to load ksFile." + e.getMessage());
            System.exit(1);
        }
    }
}
