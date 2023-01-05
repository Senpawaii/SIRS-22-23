package pt.tecnico.sirsproject.backoffice;

import java.util.HashMap;
import java.util.Map;

public class AccessControlManager {
    private final Map<String, Integer> accessLevel;

    public AccessControlManager() {
        this.accessLevel = new HashMap<>();
        populateAccessControl();
    }

    private void populateAccessControl() {
        // This method is only used to demonstrate the capabilities of this project.
        accessLevel.put("Joao", 2);
        accessLevel.put("Carlota", 2);
        accessLevel.put("Jacare", 2);
        accessLevel.put("Joana", 2);
        accessLevel.put("Antonio", 1);
        accessLevel.put("Carmina", 1);
    }

    public boolean hasAccess(String username, Integer level) {
        // Check if the user has clearance
        Integer levelUser = accessLevel.get(username);
        if(levelUser != null) {
            return levelUser.equals(level);
        }
        System.out.println("Error: a user that is not registered managed to request for access control. Not supposed to happen.");
        return false;
    }


}
