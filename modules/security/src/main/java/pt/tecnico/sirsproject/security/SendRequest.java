package pt.tecnico.sirsproject.security;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class SendRequest {
    public static String sendRequest(String address, String port, String json_req, String type_req, String handler,
                                     TrustManager[] trustManagers)
            throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String https_URL = "https://" + address + ":" + port + handler;
        URL url;
        try {
            url = new URL(https_URL);
        } catch (MalformedURLException e) {
            System.out.println("Error: Malformed URL. " + e.getMessage());
            throw new MalformedURLException();
        }

        SSLContext sslContext = TLS_SSL.createSSLContext(trustManagers, null); // TODO: KeyManager should be initialized anyways

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setSSLSocketFactory(sslContext.getSocketFactory());

        try {
            con.setRequestMethod(type_req);
        } catch (ProtocolException e) {
            System.out.println("Error: TCP error. " + e.getMessage());
            throw new ProtocolException();
        }

        con.setRequestProperty("Content-Type","application/json");
        con.setRequestProperty("Content-Length", String.valueOf(json_req.length()));
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/46.0.2490.80");

        // Send the request body
        con.setDoOutput(true);
        OutputStream os;
        try {
            os = con.getOutputStream();
            if (os != null) {
                os.write(json_req.getBytes());
                os.flush();
                os.close();
            } else {
                System.out.println("Error: Couldn't read the response.");
            }
        } catch (IOException e) {
            System.out.println("Error: I/O error. " + e.getMessage());
            e.printStackTrace();
            throw new IOException();
        }

        // Read the response
        if (con.getContentLength() > 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Print the response
            // System.out.println(response);
            return new String(response);
        }

        // error codes
        switch (con.getResponseCode()) {
            case 503:
                return "Http Error: 503 Unavailable";
            case 405:
                return "Http Error: 405 Worng Method";
            case 403:
                return "Http Error: 403 Forbidden";
            case 400:
                return "Http Error: 400 Bad Request";
        }

        return "";
    }
}
