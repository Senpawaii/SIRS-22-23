package pt.tecnico.sirsproject.security;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

public class RSAUtils {
    public static KeyStore loadKeyStoreCertificates(HashMap<String, String> certificate_paths) {
        KeyStore keyStore = createKeyStore();

        for(Map.Entry<String, String> entry : certificate_paths.entrySet()) {
            String certificatePath = entry.getValue();
            String certificateAlias = entry.getKey();

            FileInputStream fileInput = null;
            try {
                fileInput = new FileInputStream(certificatePath);
            } catch (FileNotFoundException e) {
                System.out.println("Error: couldn't find Certificate: " + certificatePath + ". " + e.getMessage());
                System.exit(1);
            }

            Certificate certificate = null;
            try {
                certificate = CertificateFactory.getInstance("X.509").generateCertificate(fileInput);
            } catch (CertificateException e) {
                System.out.println("Error: Parsing Certificate: " + certificatePath + " ." + e.getMessage());
            }

            try {
                keyStore.setCertificateEntry(certificateAlias, certificate);
            } catch (KeyStoreException e) {
                System.out.println("Error: " + certificateAlias + " Invalid certificate/ Certificate with this name " +
                        "already exists/ Other " + e.getMessage());
                System.exit(1);
            }
        }
        return keyStore;
    }

    private static KeyStore createKeyStore() {
        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            System.out.println("Error: Failed to obtain a JKS keystore. " + e.getMessage());
            System.exit(1);
        }
        try {
            keystore.load(null, null); // Load an empty KeyStore
        } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
            System.out.println("Error: Could not load empty KeyStore. " + e.getMessage());
            System.exit(1);
        }
        return keystore;
    }

    public static TrustManager[] loadTrustManagers(KeyStore keystoreCertificates) {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Provider does not support chosen algorithm for TrustManager." + e.getMessage());
            System.exit(1);
        }
        try {
            trustManagerFactory.init(keystoreCertificates);
        } catch (KeyStoreException e) {
            System.out.println("Error: Couldn't initialize TrustManagerFactory. " + e.getMessage());
        }
        return trustManagerFactory.getTrustManagers();
    }

    public static RSAPublicKey loadPublicKey(File file) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA");
        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(pubKeySpec);
        }
    }
}
