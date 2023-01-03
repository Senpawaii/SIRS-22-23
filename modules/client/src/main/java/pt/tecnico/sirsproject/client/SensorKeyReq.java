package pt.tecnico.sirsproject.client;

public class SensorKeyReq {
    private final String type;
    private final String session_token;

    public SensorKeyReq(String type, String session_token) {
        this.type = type;
        this.session_token = session_token;
    }
}
