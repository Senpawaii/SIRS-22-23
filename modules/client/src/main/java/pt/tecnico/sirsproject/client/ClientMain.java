package pt.tecnico.sirsproject.client;

import javax.crypto.SecretKey;

public class ClientMain {
    public static void main(String[] args) {
        ClientComms comms = new ClientComms();
        SecretKey sharedKey;
        GUI.printPrompt();
        while(true) {
            String[] user_pass_hash = GUI.authenticationPrompt();

            SessionToken token = comms.verify_credentials(user_pass_hash[0], user_pass_hash[1]);
            if(token != null) {
                Client client = new Client(user_pass_hash[0], user_pass_hash[1]);

                selectionMenu:
                while(true) {
                    String action = GUI.printSelectionMenu(client.getUsername());
                    switch (action) {
                        case "A1": // Ask for symmetric key to use with Sensors/ Actuators
                            try{
                                sharedKey = comms.requestSymmetricKey(token);
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