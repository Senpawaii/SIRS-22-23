package pt.tecnico.sirsproject.security;

public class UpdateSensorsKeyRequest {
    private String new_key = "dummy";
    private String p_prime = "23";
    private String g_root = "5";
    private String bigA = "4";

    public UpdateSensorsKeyRequest(String new_key) {
        this.new_key = new_key;
    }

    public UpdateSensorsKeyRequest(String p, String g, String A) {
        this.p_prime = p;
        this.g_root = g;
        this.bigA = A;
    }

    public String getNew_key() {
        return this.new_key;
    }

    public void setNew_key(String new_key) {
        this.new_key = new_key;
    }

    public String getP_prime() {
        return this.p_prime;
    }

    public void setP_prime(String p) {
        this.p_prime = p;
    }

    public String getG_root() {
        return this.g_root;
    }

    public void setG_root(String g) {
        this.g_root = g;
    }

    public String getBigA() {
        return this.bigA;
    }

    public void setBigA(String A) {
        this.bigA = A;
    }
}
