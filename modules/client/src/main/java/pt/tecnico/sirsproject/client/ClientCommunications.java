package pt.tecnico.sirsproject.client;

import pt.tecnico.sirsproject.security.SendRequest;

import javax.net.ssl.TrustManager;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public final class ClientCommunications {
    private ClientCommunications() { }

    public static String connect_to_backoffice(String request, String requestType, String handler, String address,
                                               String port, TrustManager[] trustManagers) {
        System.out.println("Connecting to BackOffice on address " + address + " and port: " + port + "...");
        String result = "";
        try {
            result = SendRequest.sendRequest(address, port, request, requestType, handler, trustManagers);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            System.out.println("Error: Failed to connect to the BackOffice.");
            System.exit(1);
        }
        return result;
    }
}
