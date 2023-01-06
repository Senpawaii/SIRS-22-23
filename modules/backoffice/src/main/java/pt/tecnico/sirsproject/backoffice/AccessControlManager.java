package pt.tecnico.sirsproject.backoffice;

import java.util.HashMap;
import java.util.Map;

public class AccessControlManager {
    private final Map<String, Integer> accessLevel;

    public static final Integer ACCESS_LEVEL_ENGINEER = 2;
    public static final Integer ACCESS_LEVEL_EMPLOYEE = 1;

    public AccessControlManager() {
        this.accessLevel = new HashMap<>();
        populateAccessControl();
    }

    private void populateAccessControl() {
        // This method is only used to demonstrate the capabilities of this project.
        accessLevel.put("Joao", ACCESS_LEVEL_ENGINEER);
        accessLevel.put("Carlota", ACCESS_LEVEL_ENGINEER);
        accessLevel.put("Jacare", ACCESS_LEVEL_ENGINEER);
        accessLevel.put("Joana", ACCESS_LEVEL_ENGINEER);
        accessLevel.put("Antonio", ACCESS_LEVEL_EMPLOYEE);
        accessLevel.put("Carmina", ACCESS_LEVEL_EMPLOYEE);
    }

    public boolean hasAccess(String username, Integer level) {
        // Check if the user has clearance

        // ===TESTING ONLY===
        // if (username.equals("admin")) return true;

        Integer levelUser = accessLevel.get(username);
        if(levelUser != null) {
            return levelUser.equals(level);
        }
        System.out.println("Error: a user that is not registered managed to request for access control. Not supposed to happen.");
        return false;
    }


}
