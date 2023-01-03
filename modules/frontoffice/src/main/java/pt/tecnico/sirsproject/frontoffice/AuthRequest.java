package pt.tecnico.sirsproject.frontoffice;

public class AuthRequest {
    private String type;
    private String encrypted_shared_key;
    private String encrypted_username;
    private String encrypted_hash_password;
    private String unencrypted_shared_key;
    private String encrypted_shared_key_no_64;

    public String getEncrypted_shared_key() {
        return encrypted_shared_key;
    }

    public void setEncrypted_shared_key(String encrypted_shared_key) {
        this.encrypted_shared_key = encrypted_shared_key;
    }

    public String getEncrypted_username() {
        return encrypted_username;
    }

    public void setEncrypted_username(String encrypted_username) {
        this.encrypted_username = encrypted_username;
    }

    public String getEncrypted_hash_password() {
        return encrypted_hash_password;
    }

    public void setEncrypted_hash_password(String encrypted_hash_password) {
        this.encrypted_hash_password = encrypted_hash_password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getEncrypted_shared_key_no_64() { return encrypted_shared_key_no_64;}
}
