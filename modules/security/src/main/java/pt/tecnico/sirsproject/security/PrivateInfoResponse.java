package pt.tecnico.sirsproject.security;

public class PrivateInfoResponse {
    private String salary;
    private String absentWorkingDays;
    private String parentalLeaves;
    private String extra_message;

    public PrivateInfoResponse(String stats, String shifts) {
        this.salary = salary;
        this.absentWorkingDays = absentWorkingDays;
        this.parentalLeaves = parentalLeaves;
    }

    public String getExtra_message() { return extra_message; }

    public String getSalary() {
        return this.salary;
    }

    public String getAbsentWorkingDays() {
        return this.absentWorkingDays;
    }

    public String getParentalLeaves() {
        return this.parentalLeaves;
    }
}
