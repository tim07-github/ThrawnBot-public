package com.tim07.thrawnbot;

import com.google.gson.JsonObject;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

/**
 * The Affirmation file shall make us happy :)
 * @author u/tim07
 * @see Message, containing affirmations
 */

public class Affirmation implements MessageCreateListener {
    /**
     * Listener on message being pushed into the server channel
     * @param event message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        // Checks if message is meant to be here...
        if (message.getContent().equals("%affirmation")){
            // API request
            JsonObject root;
            try{
                root = JSONHandler.getJSON("https://www.affirmations.dev/");
                if (root == null){
                    throw new NullPointerException("Rootobjekt ist unerwarteterweise null.");
                }
            }catch(Exception e){
                event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                return;
            }
            // JSON parsing starts here
            String affirmation = root.get("affirmation").getAsString();
            // Send message
            event.getChannel().sendMessage(affirmation);
        }
    }
}