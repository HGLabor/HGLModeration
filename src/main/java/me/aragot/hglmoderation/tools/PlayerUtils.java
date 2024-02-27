package me.aragot.hglmoderation.tools;

import org.bson.Document;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class PlayerUtils {

    public static String getUsernameFromUUID(UUID uuid){
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int status = connection.getResponseCode();

            if(status > 299) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer content = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();
            Document doc = Document.parse(content.toString());

            return doc.getString("name");

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
