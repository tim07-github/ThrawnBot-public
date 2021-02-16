package com.tim07.thrawnbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * The Animal file shall request photos and relevant information from thedogapi and thecatapi - Neat api sources I wanted to implement :)
 * @author u/tim07
 * @see EmbedBuilder
 */

public class AnimalPic extends AbstractMessageCreateListener {

    /**
     * Listener on message being pushed into the server channel
     * @param event message with extra information
     */

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        super.onMessageCreate(event);
        if (super.blacklisted){
            return;
        }
        if (event.getMessageContent().equalsIgnoreCase("%wuff")){
            wuff(event);
        }

        else if (event.getMessageContent().equalsIgnoreCase("%miau")){
            miau(event);
        }
    }

    /**
     * Processes the api call for dogs
     * @param event {@link MessageCreateEvent}
     */
    private void wuff(MessageCreateEvent event){
        String urlString = "https://api.thedogapi.com/v1/images/search?has_breeds=true&limit=1";
        String apikey = ThrawnBot.property.getProperty("API_KEY_DOG");
        Map<String, JsonObject> main = processAPI(urlString, apikey, event);

        if (main != null){
            JsonObject breed = main.values().stream().findFirst().orElse(null);
            if (breed == null){
                return;
            }
            String name = breed.get("name").getAsString();
            String temperament = breed.get("temperament").getAsString();
            String url = main.keySet().stream().findFirst().orElse("");
            // Create EmbedBuilder with params
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("TheDogApi")
                    .addInlineField("Beschreibung:", temperament)
                    .setTitle(name)
                    .setImage(url).setColor(Color.green);
            // Send message
            event.getChannel().sendMessage(embedBuilder);
        }
    }

    /**
     * Processes the API Call for cats.
     * @param event {@link MessageCreateEvent}
     */
    private void miau(MessageCreateEvent event){
        String urlString = "https://api.thecatapi.com/v1/images/search?has_breeds=true&limit=1";
        String apikey = ThrawnBot.property.getProperty("API_KEY_CAT");
        Map<String, JsonObject> main = processAPI(urlString, apikey, event);


        if (main != null){
            JsonObject breed = main.values().stream().findFirst().orElse(null);
            if (breed == null){
                return;
            }
            String name = breed.get("name").getAsString();
            String temperament = breed.get("temperament").getAsString();
            String description = breed.get("description").getAsString();
            String url = main.keySet().stream().findFirst().orElse("");

            // Create EmbedBuilder with params
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setAuthor("TheCatApi")
                    .setDescription(description)
                    .addInlineField("Beschreibung:", temperament)
                    .setTitle(name)
                    .setImage(url).setColor(Color.green);
            // Send message
            event.getChannel().sendMessage(embedBuilder);
        }
    }


    /**
     * Processes the API Request and returns the "breed" Object, a part of the main JSON file transmitted
     * @param urlString Requested URL
     * @param apikey API-Key
     * @return {@link Map} Url and the JSONObject or {@literal null}
     */
    private Map<String, JsonObject> processAPI(String urlString, String apikey, MessageCreateEvent event){
        // API fetch
        JsonArray root;

        try {
            root = getJsonArray(urlString, apikey);

            if (root == null ||root.size() == 0){
                throw new NullPointerException("JSON: Rootobjekt ist unerwarteterweise null.");
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
            return null;
        }
        //Main JSON Object
        JsonObject main = root.get(0).getAsJsonObject();

        JsonArray breeds = main.get("breeds").getAsJsonArray();

        try{
            if (breeds.size() == 0){
                throw new IllegalStateException("JSON: Die Datei hat keine Infos über das Tier.");
            }
        }catch(Exception e){
            event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
            return null;
        }

        String url = main.get("url").toString().replace("\"", "");

        Map<String, JsonObject> returnedMap = new HashMap<>();
        returnedMap.put(url, breeds.get(0).getAsJsonObject());

        return returnedMap;
    }


    /**
     * Same methode as JSONHandler.getJSON, but with arrays
     * @param urlString String, f.ex. the url to an api request
     * @param apikey apikey used to access the api
     * @return JSONArray, which can be used to access the params for f.ex. messages
     * @throws IOException, if the url connection or Jsonparsing fails
     */

    private JsonArray getJsonArray(String urlString, String apikey) throws IOException {
        URL url = new URL(urlString);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.addRequestProperty("X-API-KEY", apikey);

        httpsURLConnection.connect();

        if (httpsURLConnection.getResponseCode() != 200){
            throw new IllegalArgumentException("API: Die API hat zur Zeit Probleme deine Eingabe zu verstehen.");
        }
        //String parsing
        StringBuilder inline = new StringBuilder();
        Scanner sc = new Scanner(url.openStream(), StandardCharsets.UTF_8);
        while(sc.hasNext()) {
            inline.append(sc.nextLine());
        }
        sc.close();

        return (JsonArray) JsonParser.parseString(inline.toString());
    }
}
