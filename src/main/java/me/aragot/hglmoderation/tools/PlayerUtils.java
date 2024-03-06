package me.aragot.hglmoderation.tools;

import org.bson.Document;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class PlayerUtils {

    private static final HashMap<String, String> playerNameCache = new HashMap<>();

    public static String getUsernameFromUUID(String uuid){
        if(playerNameCache.containsKey(uuid))
            return playerNameCache.get(uuid);

        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int status = connection.getResponseCode();

            if(status > 299) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();
            Document doc = Document.parse(content.toString());
            playerNameCache.put(uuid, doc.getString("name"));
            return doc.getString("name");

        } catch (IOException e) {
            return "null";
        }
    }

    public static String getUuidFromUsername(String username){
        if(playerNameCache.containsKey(username))
            return playerNameCache.values().stream()
                    .filter(name -> Objects.equals(name, username))
                    .findFirst().orElse("None");

        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int status = connection.getResponseCode();

            if(status > 299) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();
            Document doc = Document.parse(content.toString());

            return addHyphensToUUID(doc.getString("id"));

        } catch (IOException e) {
            return null;
        }
    }

    public static String addHyphensToUUID(String uuidWithoutHyphens) {
        if (uuidWithoutHyphens.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID length");
        }

        StringBuilder sb = new StringBuilder(uuidWithoutHyphens);
        sb.insert(20, "-");
        sb.insert(16, "-");
        sb.insert(12, "-");
        sb.insert(8, "-");

        return sb.toString();
    }

    public static void removePlayerFromCache(String uuid){
        playerNameCache.remove(uuid);
    }
}
