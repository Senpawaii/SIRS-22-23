package pt.tecnico.sirsproject.security;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class SendRequest {
    public static String sendRequest(String address, String port, String json_req, String type_req, String handler, File crtFile) throws IOException {
        String https_URL = "https://" + address + ":" + port + handler;
        URL url = null;
        try {
            url = new URL(https_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert url != null;

        Certificate certificate = null;
        try {
            certificate = CertificateFactory.getInstance("X.509").generateCertificate(new FileInputStream(crtFile));
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null, null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        try {
            keyStore.setCertificateEntry("server", certificate);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            trustManagerFactory.init(keyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(sslContext.getSocketFactory());

        try {
            con.setRequestMethod(type_req);
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        con.setRequestProperty("Content-length", String.valueOf(json_req.length()));
        con.setRequestProperty("Content-Type","application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/46.0.2490.80");

        // Send the request body
        con.setDoOutput(true);
        OutputStream os = null;
        try {
            os = con.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (os != null) {
            try {
                os.write(json_req.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        assert os != null;
        try {
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print the response
        System.out.println(response);
        return new String(response);
    }
}
