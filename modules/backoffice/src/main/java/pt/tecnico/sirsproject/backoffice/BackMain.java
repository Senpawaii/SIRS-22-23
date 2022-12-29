package pt.tecnico.sirsproject.backoffice;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsConfigurator;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLContext;

import pt.tecnico.sirsproject.backoffice.BackHandlers.PingHandler;
import pt.tecnico.sirsproject.backoffice.BackHandlers.AuthenticateHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Properties;

public class BackMain {
    public static void main(String[] args) {

        SessionManager manager = new SessionManager();
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

        // Load properties file
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("../../extra_files/backoffice/config.properties")); // TODO: Find a more reliable way of using relative paths
        } catch (IOException e) {
            System.out.println("Error reading properties file: " + e.getMessage());
            System.exit(-1);
        }


        // Start HTTPS server
        String password = properties.getProperty("keystore_pass");
        try {
            System.out.println("Starting server on port " + port + "...");

            HttpsServer server = createTLSServer(port, ksFile, password);
            server.createContext("/test", new PingHandler());
            server.createContext("/auth", new AuthenticateHandler(manager));
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

    private static HttpsServer createTLSServer(int port, String ksFile, String password) 
        throws TLSServerException {
        
        InetSocketAddress address = new InetSocketAddress(port);

        // Initialize the server
        HttpsServer server;
        try {
            server = HttpsServer.create(address, 0);
        } catch (IOException e) {
            throw new TLSServerException("Error: Couldn't create server on requested port " + port + ".", e);
        }

        // Initialize the SSL context
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: Couldn't find requested SSL Context algorithm.", e);
        }

        // Initialize the keystore
        char[] pass = password.toCharArray();
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(ksFile), pass);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new TLSServerException("Error: Couldn't load KeyStore.", e);
        }

        // Initialize the key and trust manager factories
        KeyManagerFactory keyManager;
        TrustManagerFactory trustManager;

        try {
            keyManager = KeyManagerFactory.getInstance("SunX509");
            trustManager = TrustManagerFactory.getInstance("SunX509");
            keyManager.init(keyStore, pass);
            trustManager.init(keyStore);
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
            throw new TLSServerException("Error: Couldn't initialize key or trust manager factory.", e);
        }

        // Initialize the SSL context with the key and trust managers
        try {
            context.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            throw new TLSServerException("Error: Unable to initialize SSL context.", e);
        }

        // Set the SSL context for the backoffice server
        server.setHttpsConfigurator(new HttpsConfigurator(context) {
            public void configure(HttpsParameters params) {
                try {
                    SSLContext sslcontext = SSLContext.getDefault();
                    SSLEngine engine = sslcontext.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // set SSL parameters
                    SSLParameters sslparams = sslcontext.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                    System.out.println("Server connected on port " + port + ".");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    System.out.println("Failed to create server.");
                }
            }
        });
        return server;
    }
}
