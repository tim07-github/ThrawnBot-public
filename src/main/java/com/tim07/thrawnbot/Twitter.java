package com.tim07.thrawnbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.javacord.api.event.message.MessageCreateEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.net.http.HttpRequest;

/** This file should provide valid links to various twitter accounts and post them.
 * @author u/tim07
 */

public class Twitter extends AbstractMessageCreateListener{

    private final Random random = new Random();
    /**
     * Listener on message pushed into the server channel
     * @param event message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        super.onMessageCreate(event);
        if (super.blacklisted){
            return;
        }
        if (event.getMessageContent().equalsIgnoreCase("%nachrichten")){
            try {


                String auth = ThrawnBot.property.getProperty("API_KEY_TWITTER");

                var client = HttpClient.newHttpClient();

                var request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create("https://api.twitter.com/2/tweets/search/recent?query=from:BBCBweaking"))
                        .header("Authorization", auth)
                        .build();

               var response = client.send(request, HttpResponse.BodyHandlers.ofString());

               JsonElement root = JsonParser.parseString(response.body());
               JsonArray data = root.getAsJsonObject().getAsJsonArray("data");

               List<String> ids = new LinkedList<>();

               for (int i = 0; i < data.size(); i++){
                   JsonObject temp = data.get(i).getAsJsonObject();
                    ids.add(temp.get("id").getAsString());
                }

                int index = random.nextInt(ids.size());
                event.getChannel().sendMessage("https://twitter.com/BBCBweaking/status/" + ids.get(index));
            } catch (Exception e){
                event.getChannel().sendMessage("Fehler mit Nachricht: Das Twittermodul scheint Probleme zu haben.");
            }
        }
    }
}
