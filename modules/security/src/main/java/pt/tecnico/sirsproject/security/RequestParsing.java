package pt.tecnico.sirsproject.security;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpsExchange;

import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RequestParsing {
    public static CredentialsRequest parseCredentialsRequestToJSON(HttpsExchange exc) throws IOException {
        InputStreamReader isr = new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String requestBody = removeQuotesAndUnescape(br.readLine());

        Gson gson = new Gson();
        try {
            return gson.fromJson(requestBody, CredentialsRequest.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static SensorKeyRequest parseSensorKeyRequestToJSON(HttpsExchange exc) throws IOException {
        InputStreamReader isr = new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String requestBody = removeQuotesAndUnescape(br.readLine());

        Gson gson = new Gson();
        try {
            return gson.fromJson(requestBody, SensorKeyRequest.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static UpdateSensorsKeyRequest parseUpdateSensorsKeyRequestToJSON(HttpsExchange exc) throws IOException {
        InputStreamReader isr = new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String requestBody = removeQuotesAndUnescape(br.readLine());

        Gson gson = new Gson();
        try {
            return gson.fromJson(requestBody, UpdateSensorsKeyRequest.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static ClientSensorsRequest parseClientSensorsRequestToJSON(HttpsExchange exc, String key) throws IOException, SensorsDecryptionException {
        InputStreamReader isr = new InputStreamReader(exc.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String requestBody = removeQuotesAndUnescape(br.readLine());

        String decryptedRequest = SymmetricKeyEncryption.decrypt(requestBody, key);
        if (decryptedRequest == "") {
            // unable to decrypt, probably wrong key
            throw new SensorsDecryptionException("Unable to decrypt client request.");
        }

        Gson gson = new Gson();
        try {
            return gson.fromJson(requestBody, ClientSensorsRequest.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    static String removeQuotesAndUnescape(String uncleanJson) {
        String noQuotes = uncleanJson.replaceAll("^\"|\"$", "");

        return StringEscapeUtils.unescapeJava(noQuotes);
    }
}
