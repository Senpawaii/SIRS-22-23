package pt.tecnico.sirsproject.client;

public class ClientMain {
    public static void main(String[] args) {
        ClientComms comms = new ClientComms();

        GUI.printPrompt();
        while(true) {
            String[] user_pass_hash = GUI.authenticationPrompt();

            boolean access = comms.verify_credentials(user_pass_hash[0], user_pass_hash[1]);
            if(access) {
                Client client = new Client(user_pass_hash[0], user_pass_hash[1]);

                while(true) {
                    String action = GUI.printSelectionMenu(client.getUsername());
                    switch (action) {
                        case "A1":
                            System.out.println("A1");
                            break;
                        case "A2":
                            System.out.println("A2");
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