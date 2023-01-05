package pt.tecnico.sirsproject.frontoffice;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bouncycastle.util.encoders.Hex;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static com.mongodb.client.model.Filters.eq;

public final class DatabaseCommunications {
    private static final int ITERATIONS = 1000;
    private static final int SALT_SIZE = 512;

    public static boolean validate_credentials(String username, String password, MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("Users");
        MongoCollection<Document> collection = database.getCollection("user_pass");

        Document doc = collection.find(eq("user", username)).first();
        if (doc != null) {
            System.out.println(doc.toJson());
        } else {
            System.out.println("No matching documents found.");
            return false;
        }
        String hash_hex = (String) doc.get("hash");

        String salt_hex = (String) doc.get("salt");
        byte[] salt = Hex.decode(salt_hex);
        char[] password_array = password.toCharArray();
        String computed_password = getPasswordHexHash(password_array, salt);

        return computed_password.equals(hash_hex);
    }

    public static void populateDBUsers(String[] usernames, String[] passwords, MongoClient mongoClient) {
        for(int i = 0; i <  usernames.length; i++) {
            String username = usernames[i];
            char[] password = passwords[i].toCharArray();

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_SIZE];
            random.nextBytes(salt);

            String hash_hex = getPasswordHexHash(password, salt);
            String salt_hex = Hex.toHexString(salt);
            storeDocument(username, hash_hex, salt_hex, mongoClient);
        }
    }

    private static String getPasswordHexHash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, SALT_SIZE);
        byte[] hash_bytes = new byte[0];
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            hash_bytes = skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error: Couldn't generate hash for password. " + e.getMessage());
            System.exit(1);
        }
        return Hex.toHexString(hash_bytes);
    }

    private static void storeDocument(String username, String hash_hex, String salt_hex, MongoClient mongoClient) {
        MongoDatabase users_database = mongoClient.getDatabase("Users");
        MongoCollection<Document> users_collection = users_database.getCollection("users_pass");
        Document user_pass = new Document("_id", new ObjectId());
        user_pass.append("user", username)
                 .append("hash",hash_hex)
                 .append("salt", salt_hex);
        users_collection.insertOne(user_pass);
    }


}

