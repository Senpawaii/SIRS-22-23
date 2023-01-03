package pt.tecnico.sirsproject.client;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HTTPSClient {
    private Certificate serverCert;
    private SSLSocketFactory sslSocketFactory;
    SSLSocket sslSocket;

    public HTTPSClient(String address, String port) throws IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        // Load the server's certificate from a file
        FileInputStream file = new FileInputStream("../../extra_files/backoffice/server.crt");
        serverCert = CertificateFactory.getInstance("X.509").generateCertificate(file);
        file.close();

        // Initialize the SSL context and socket factory
        initSSLContext();

        // Connect to the server
        sslSocket = connectToServer(address, port);
    }

    private void initSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a TrustManager that trusts the server's certificate
        TrustManager[] trustServerCert = new TrustManager[] {
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // Trust all clients
                    }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // Trust only the server with the given certificate
                        if (chain[0].equals(serverCert)) {
                            return;
                        }
                        throw new CertificateException("Untrusted server certificate");
                    }
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };

        // Initialize the SSL context with the trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustServerCert, new SecureRandom());

        // Create an SSL socket factory using the SSL context
        sslSocketFactory = sslContext.getSocketFactory();
    }

    private SSLSocket connectToServer(String host, String port) throws IOException {
        return (SSLSocket) sslSocketFactory.createSocket(host, Integer.parseInt(port));
    }

    private void sendRequest(String request) throws IOException {
        OutputStream outputStream = sslSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream));
        writer.print(request);
        writer.flush();
    }

    private String readResponse() throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append("\n");
        }
        return response.toString();
    }

    private void closeConnection() throws IOException {
        sslSocket.close();
    }
}
