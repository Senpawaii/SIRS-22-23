package pt.tecnico.sirsproject.client;

public class ClientMain {
    public static void main(String[] args) {
        Client client = new Client();

        GUI.printPrompt();
        while(true) {
            String[] user_pass = GUI.authenticationPrompt();
            client.setIdentity(user_pass[0], user_pass[1]);

            if(client.verify_credentials()) {
                selectionMenu:
                while(true) {
                    String action = GUI.printSelectionMenu(client.getUsername());
                    switch (action) {
                        case "A1": // Ask for symmetric key to use with Sensors/ Actuators
                            try{
                                client.obtainSensorKey();
                            } catch (Exception e) {
                                System.out.println("Session token invalid/ expired. Please authenticate again");
                                break selectionMenu;
                            }
                            System.out.println("A1");
                            break;
                        case "A2": // Query stock
                            System.out.println("A2");
                            break;
                        case "A3": // Buy parts
                            System.out.println("A3");
                            break;
                        case "C1": // Contact Sensors
                            client.accessSensors();
                            break;
                        case "4":
                            System.out.println("Exiting...");
                            System.exit(0);
                        default:
                            throw new IllegalStateException("Unexpected value: " + action);
                    }
                }
            } else {
                System.out.println("Incorrect credentials. Please try again.");
            }
        }
    }
}