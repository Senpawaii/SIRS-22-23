package pt.tecnico.sirsproject.frontoffice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;


public class GenerateJKS {
    public GenerateJKS() {
        String password = "frontoffice";
        PrivateKey privateKey = null;
        try {
            privateKey = getPrivateKeyFromDER("../../extra_files/frontoffice/private_key.der");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        X509Certificate certificate = null;
        try {
            certificate = getCertificateFromPEM("../../extra_files/frontoffice/server.crt");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }

        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keystore.load(null, null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
        try {
            keystore.setKeyEntry("private_key", privateKey, password.toCharArray(), new Certificate[] { certificate });
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keystore.setCertificateEntry("certificate", certificate);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("../../extra_files/frontoffice/frontoffice.jks");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            keystore.store(fos, password.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey getPrivateKeyFromDER(String filename) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException {
        FileInputStream fis = new FileInputStream(filename);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);
        fis.close();

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        try {
            return keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static X509Certificate getCertificateFromPEM(String filename) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        FileInputStream fis = new FileInputStream(filename);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fis);
        fis.close();
        return certificate;
    }

}
