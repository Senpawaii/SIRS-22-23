package pt.tecnico.sirsproject.frontoffice;

import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsConfigurator;
// import com.sun.net.httpserver.*;

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

import pt.tecnico.sirsproject.frontoffice.FrontHandlers.PingHandler;

import java.io.*;
import java.net.InetSocketAddress;

public class FrontMain {
    public static void main(String[] args) throws IOException {

        int port = -1;
        String ksFile = "";
        String password = "";
        
        if (args.length != 3) {
            System.out.println("ARGS: <port> <keyStorePath> <keystorePassword>");
            System.exit(-1);
        }

        try {
            port = Integer.parseInt(args[0]);
            ksFile = args[1];
            password = args[2];
        } catch (IllegalArgumentException e) {
            System.out.println("ARGS: <port> <keystorePassword>");
            System.exit(-1);
        }

        try {
            System.out.println("Starting server on port " + port + "...");

            // HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            HttpsServer server = createTLSServer(port, ksFile, password);
            server.createContext("/test", new PingHandler());
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

    private static HttpsServer createTLSServer(int port, String ksFile, String password) 
        throws TLSServerException {
        
        InetSocketAddress address = new InetSocketAddress(port);

        // initialize the server
        HttpsServer server;
        try {
            server = HttpsServer.create(address, 0);
        } catch (IOException e) {
            throw new TLSServerException("Error: Couldn't create server on requested port " + port + ".", e);
        }
        SSLContext context;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: Couldn't find requested SSL Context algorithm.", e);
        }

        // initialize the keystore
        char[] pass = password.toCharArray();
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new TLSServerException("Error: Couldn't find requested KeyStore instance.", e);
        }
        FileInputStream in;
        try {
            in = new FileInputStream(ksFile);
        } catch (FileNotFoundException e) {
            throw new TLSServerException("Error: Unable to read keystore file \"" + ksFile + "\".", e);
        }
        try {
            keyStore.load(in, pass);
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new TLSServerException("Error: Unable to load keystore.", e);
        }

        // setup the key manager factory
        KeyManagerFactory keyManager;
        try {
            keyManager = KeyManagerFactory.getInstance("SunX509");
        } catch (NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: No such algorithm for KeyManagerFactory \"SunX509\"", e);
        }
        try {
            keyManager.init(keyStore, pass);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: Unable to initialize key manager.", e);
        }

        // setup the trust manager
        TrustManagerFactory trustManager;
        try {
            trustManager = TrustManagerFactory.getInstance("SunX509");
        } catch (NoSuchAlgorithmException e) {
            throw new TLSServerException("Error: No such algorithm for TrustManagerFactory \"SunX509\"", e);
        }
        try {
            trustManager.init(keyStore);
        } catch (KeyStoreException e) {
            throw new TLSServerException("Error: Unable to initialize trust manager.", e);
        }

        // setup the HTTPS context and parameters
        try {
            context.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            throw new TLSServerException("Error: Unable to initilize SSL context with given key and trust managers.", e);
        }
        server.setHttpsConfigurator(new HttpsConfigurator(context) {
            public void configure(HttpsParameters params) {
                try {
                    // initialize the SSL context
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
