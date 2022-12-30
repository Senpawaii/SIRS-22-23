package pt.tecnico.sirsproject.client;

import com.google.common.hash.Hashing;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Inputsecurity {
    public static Scanner sc = new Scanner(System.in);

    static String getUsername() {
        while (true) {
            System.out.print("Username: ");
            String username = sc.nextLine();

            if (username.isEmpty() || !Pattern.matches("^[a-zA-Z0-9]{1,10}$", username)) {
                System.out.println("Invalid username. Please try again.");
            } else {
                return username;
            }
        }
    }

    static String getHashPassword() {
//        Console console = System.console();
//        if (console == null) {
//            System.out.println("Couldn't get Console instance");
//            System.exit(1);
//        }
//        String passwordString = new String(console.readPassword("Password: "));

        System.out.print("Password: ");
        String passwordString = sc.nextLine();
        sc.close();
        return Hashing.sha256().hashString(passwordString, StandardCharsets.UTF_8).toString();
    }
}
