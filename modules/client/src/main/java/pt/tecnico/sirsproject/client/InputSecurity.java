package pt.tecnico.sirsproject.client;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputSecurity {
    static String getUsername(Scanner sc) {
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

    static String getHashPassword(Scanner sc) {
        System.out.print("Password: ");
        String passwordString = sc.nextLine();
        return Hashing.sha256().hashString(passwordString, StandardCharsets.UTF_8).toString();
    }
}
