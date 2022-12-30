package pt.tecnico.sirsproject.client;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSASecurity {
    public static String encryptSecretKey(String AESKey, PublicKey publicKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(AESKey.getBytes()));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static RSAPublicKey temp_loadPublicKey(String filename) {
//        System.out.println("Working Directory = " + System.getProperty("user.dir"));
////        Path path = Paths.get(filename);
//        Path path = Paths.get("../../extra_files/backoffice/temp_2/bank1pub.pem");
//
//        if(path.toFile().isFile()) {
//            byte[] keyBytes = new byte[0];
//            try {
//                keyBytes = Files.readAllBytes(path);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            KeyFactory kf = null;
//            try {
//                kf = KeyFactory.getInstance("RSA");
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//            }
//            X509EncodedKeySpec spec =
//                    new X509EncodedKeySpec(keyBytes);
//
//            // Use Key factory to recreate the Public key instance
//            try {
//                assert kf != null;
//                return (RSAPublicKey) kf.generatePublic(spec);
//            } catch (InvalidKeySpecException e) {
//                e.printStackTrace();
//            }
//        } else {
//            System.out.println("Error: Couldn't load the Public Key file.");
//            System.exit(1);
//        }
//        return null;
//    }

    static RSAPublicKey loadPublicKey(File file) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
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
