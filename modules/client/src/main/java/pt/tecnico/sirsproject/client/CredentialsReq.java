package pt.tecnico.sirsproject.client;

public class CredentialsReq {
    private final String type;
    private final String encrypted_shared_key;
    private final String encrypted_username;
    private final String encrypted_hash_password;

    public CredentialsReq(String type, String encrypted_username, String encrypted_hash_password, String encrypted_shared_key) {
        this.type = type;
        this.encrypted_username = encrypted_username;
        this.encrypted_hash_password = encrypted_hash_password;
        this.encrypted_shared_key = encrypted_shared_key;
    }
}
