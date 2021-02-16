package com.tim07.thrawnbot;

import com.google.gson.JsonObject;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

/**
 * The Astro file shall fetch various NASA APIs to gain some information on mars temperature or the APOD
 * @see EmbedBuilder
 */


public class AstroPhotoOfTheDay extends AbstractMessageCreateListener {


    /**
     * Listener on message being sent into server channel
     * @param event message with extra information
     */

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        super.onMessageCreate(event);
        if (super.blacklisted){
            return;
        }
        //Checks if message is meant to be here..
        if (event.getMessageContent().equals("%astro")){
            String apiKey = ThrawnBot.property.getProperty("API_KEY_NASA");
            astro(event, apiKey);
        }
    }

    private void astro(MessageCreateEvent event, String apiKey){

        String urlString = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;
        // API fetch
        JsonObject root = requestJSON(event, urlString);
        if (root == null){
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
    }

    private JsonObject requestJSON(MessageCreateEvent event, String urlString){
        JsonObject root;
        try {
            root = JSONHandler.getJSON(urlString);
            if (root == null){
                throw new NullPointerException("JSON: Rootobjekt unerwarteterweise Null.");
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
            return null;
        }
        return root;
    }
}
