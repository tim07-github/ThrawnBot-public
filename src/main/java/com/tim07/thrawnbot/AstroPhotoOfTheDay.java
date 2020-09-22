package com.tim07.thrawnbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;

/**
 * The Astro file shall fetch various NASA APIs to gain some information on mars temperature or the APOD
 * @see EmbedBuilder
 */


public class AstroPhotoOfTheDay implements MessageCreateListener {

    /**
     * Listener on message being sent into server channel
     * @param event message with extra information
     */

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        String apiKey = "APIKEY HERE";

        //Checks if message is meant to be here..
        if (message.getContent().equals("%astro")){

            String urlString = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;

            // API fetch
            JsonObject root;
            try {
                root = JSONHandler.getJSON(urlString);
                if (root == null){
                    throw new NullPointerException("JSON: Rootobjekt unerwarteterweise Null.");
                }
            } catch (Exception e) {
                event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                return;
            }
            // JSON Parsing starts here
            try {
                String copyright;

                if (root.get("copyright") == null){
                    copyright = "NASA";
                }else{
                    copyright = root.get("copyright").getAsString();
                }
                String date = root.get("date").getAsString();
                String title = root.get("title").getAsString();
                String url = root.get("url").getAsString();
                String description = root.get("explanation").getAsString();
                // Create EmbedBuilder with params
                EmbedBuilder embedBuilder = new EmbedBuilder()
                        .setAuthor(copyright)
                        .setDescription(description)
                        .setImage(url)
                        .setTitle(title)
                        .setFooter(date)
                        .setColor(Color.black);
                // Send message
                event.getChannel().sendMessage(embedBuilder);
            }catch(Exception e) {
                event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Beim Auslesen des Objekts ist ein Fehler aufgetreten.");
            }

        }else if (message.getContent().equals("%astro marswetter")){
            String urlString = "https://api.nasa.gov/insight_weather/?api_key=" + apiKey + "&feedtype=json&ver=1.0";
            // API fetch
            JsonObject root;
            try {
                root = JSONHandler.getJSON(urlString);
                if (root == null){
                    throw new NullPointerException("JSON: Rootobjekt unerwarteterweise Null.");
                }
            } catch (Exception e) {
                event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                return;
            }
            // JSON parsing begins here
            try {
                // Create EmbedBuilder with params
                EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.black).setTitle("Marswetter").setAuthor("NASA");
                JsonArray keys = root.get("sol_keys").getAsJsonArray();
                String season = "";

                for (int i = 0; i < keys.size(); i++){
                    String key = keys.get(i).getAsString().replaceAll("\"", "");
                    JsonObject main = root.get(key).getAsJsonObject();
                    JsonObject temperature = main.get("AT").getAsJsonObject();
                    String temp = temperature.get("av").getAsString();
                    season = main.get("Season").getAsString();
                    embedBuilder.addInlineField("Temperatur auf SOL" + key, temp);
                }
                embedBuilder.setFooter("Jahreszeit: " + season + "; Angaben in Grad Celsius.");
                // Send EmbedBuilder
                event.getChannel().sendMessage(embedBuilder);
            }catch(Exception e) {
                event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Beim Auslesen des Objekts ist ein Fehler aufgetreten.");
            }
        }
    }
}
