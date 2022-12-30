package pt.tecnico.sirsproject.client;

import java.util.Scanner;

public class GUI {
    public static void printPrompt() {
        System.out.println("==================================");
        System.out.println("Welcome to StarDrive Systems");
        System.out.println("==================================");
        System.out.println();
    }

    public static String[] authenticationPrompt() {
        System.out.println("Please authenticate yourself: ");
        String username = Inputsecurity.getUsername();
        String hashedPassword = Inputsecurity.getHashPassword();
        return new String[] {username, hashedPassword};
    }

    public static String printSelectionMenu(String username) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("-----------------------------------");
            System.out.println("Hello " + username + ", how can we help you today?");
            System.out.println();
            System.out.println("1 - Contact BackOffice");
            System.out.println("2 - Contact FrontOffice");
            System.out.println("3 - Contact Actuators/Sensors");
            System.out.println("4 - Quit");
            System.out.println();
            String command = scanner.nextLine();
            switch (command) {
                case "1":
                    while (true) {
                        command = printBackofficeMenu();
                        if (command.equals("1") || command.equals("2") || command.equals("3")) {
                            return "A" + command;
                        } else if (command.equals("4")) {
                            break;
                        }
                        System.out.println("Please insert a valid command.");
                    }
                    break;
                case "2":
                    System.out.println("Contact FrontOffice");
                    // comms.contactFrontoffice();
                    break;
                case "3":
                    System.out.println("Contact Actuators");
                    break;
                case "4":
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please insert a valid command.");
                    break;
            }
        }
    }

    public static String printBackofficeMenu() {
        System.out.println("-----------------------------------");
        System.out.println("=BackOffice Menu=");
        System.out.println("-----------------------------------");
        System.out.println();
        System.out.println("1 - Request Sensor Key");
        System.out.println("2 - Query stock");
        System.out.println("3 - Buy Parts");
        System.out.println("4 - Back");
        System.out.println();
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}