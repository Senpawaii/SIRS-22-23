package pt.tecnico.sirsproject.security;

public class UpdateSensorsKeyRequest extends Request {
    private String p_prime;
    private String g_root;
    private String bigA;

    public UpdateSensorsKeyRequest(String p, String g, String A) {
        this.p_prime = p;
        this.g_root = g;
        this.bigA = A;
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
