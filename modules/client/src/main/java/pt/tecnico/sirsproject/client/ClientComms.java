package pt.tecnico.sirsproject.client;

import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Properties;

public class ClientComms {
    private final Properties prop = new Properties();

    public ClientComms() {
        loadPropertiesFile();
    }

    private void loadPropertiesFile() {
        try (InputStream input = new FileInputStream("../../extra_files/client/app.properties")) {
            // load a properties file
            this.prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void connect_to_backoffice(String json_req, String type_req, String handler) {
        String address = prop.getProperty("backoffice_ip_address");
        String port = prop.getProperty("backoffice_port");

        System.out.println("Contacting Back office on address " + address + " and port: " + port + "...");
        try {
            sendRequest(address, port, json_req, type_req, handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String address, String port, String json_req, String type_req, String handler) throws IOException {
        String https_URL = "https://" + address + ":" + port + handler;
        URL url = null;
        try {
            url = new URL(https_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert url != null;

        File crtFile = new File("../../extra_files/backoffice/server.crt");
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

        // Read the response from the server
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print the response
        System.out.println(response.toString());
    }

    public boolean verify_credentials(String username, String hashed_password) {
        File publicKeyFile = new File("../../extra_files/backoffice/public.key");

        String encodedSymmetricKey = new Symmetric().getEncodedSymmetricKey();
        System.out.println("EncodedSymmetricKey:" + encodedSymmetricKey);
        PublicKey serverPublic = null;
        try {
            serverPublic = RSASecurity.loadPublicKey(publicKeyFile);
            System.out.println("Server public key:" + Base64.getEncoder().encodeToString(serverPublic.getEncoded()));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        String encryptedAESKey = RSASecurity.encryptSecretKey(encodedSymmetricKey, serverPublic);

        String type = "verify_auth";
        String encrypted_username = Symmetric.encrypt(username, encodedSymmetricKey);
        String encrypted_hash_password = Symmetric.encrypt(hashed_password, encodedSymmetricKey);;

        Gson gson = new Gson();
        CredentialsReq req = new CredentialsReq(type, encrypted_username, encrypted_hash_password, encryptedAESKey);

        String json_req = gson.toJson(req);
        System.out.println(json_req);

        connect_to_backoffice(json_req, "POST", "/auth");
        return false;
    }
}
