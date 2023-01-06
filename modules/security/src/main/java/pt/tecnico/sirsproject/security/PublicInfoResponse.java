package pt.tecnico.sirsproject.security;

public class PublicInfoResponse {
    private String stats;
    private String shifts;
    private String extra_message;

    public PublicInfoResponse(String stats, String shifts) {
        this.stats = stats;
        this.shifts = shifts;
    }

    public String getExtra_message() { return extra_message; }

    public String getStats() {
        return stats;
    }

    public String getShifts() {
        return shifts;
    }

    
}
