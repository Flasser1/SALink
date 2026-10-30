package dk.flasser.salink.API;

import eu.okaeri.platform.core.annotation.Component;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class SuperawesomeAPI {
    public String getDiscordID(String username)
            throws Exception {

        URL url = new URL(
                "https://api.superawesome.dk/players/" + username
        );

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty(
                "Accept", "application/json"
        );

        try {
            int status = connection.getResponseCode();

            if (status != 200) {
                throw new Exception(
                        "API returned HTTP " + status
                );
            }

            StringBuilder response = new StringBuilder();

            try (InputStream input = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(
                                 input, StandardCharsets.UTF_8
                         ))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JSONObject json = (JSONObject) new JSONParser()
                    .parse(response.toString());

            Object discordID = json.get("discordID");

            return discordID == null ? null : discordID.toString();

        } finally {
            connection.disconnect();
        }
    }
}
