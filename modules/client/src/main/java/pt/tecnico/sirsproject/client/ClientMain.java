package pt.tecnico.sirsproject.client;

import javax.crypto.SecretKey;
import java.util.Map;

public class ClientMain {
    public static void main(String[] args) {
        ClientComms comms = new ClientComms();
        Client client;

        SessionToken sessiontoken;
        String symmetric_key_backoffice;
        String sharedKey_sensors;

        GUI.printPrompt();
        while(true) {
            String[] user_pass_hash = GUI.authenticationPrompt();

            Map<String, Object> values = comms.verify_credentials(user_pass_hash[0], user_pass_hash[1]);
            sessiontoken = (SessionToken) values.get("sessionToken");
            symmetric_key_backoffice = (String) values.get("symmetricKey");
             if(sessiontoken != null) {
                 client = new Client(user_pass_hash[0], user_pass_hash[1]);

                selectionMenu:
                while(true) {
                    String action = GUI.printSelectionMenu(client.getUsername());
                    switch (action) {
                        case "A1": // Ask for symmetric key to use with Sensors/ Actuators
                            try{
                                sharedKey_sensors = comms.requestSensorKey(client.getUsername(), sessiontoken);
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