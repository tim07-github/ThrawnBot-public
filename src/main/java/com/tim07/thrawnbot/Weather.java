package com.tim07.thrawnbot;

import com.google.gson.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The weather listener should present a current weather info message in discord - fetched from the Open Weather API
 * @author u/tim07
 * @see EmbedBuilder being sent into the message channel
 */

public class Weather extends AbstractMessageCreateListener {
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

        // Checks if the message being fetched is meant to be here...
        if (event.getMessageContent().startsWith("%wetter ") && !(event.getMessageContent().equals("%wetter "))) {

            String apikey = ThrawnBot.property.getProperty("API_KEY_OPENWEATHER");

            // Converts f.ex. umlauts to a more useful format
            byte[] stringByte = event.getMessageContent().getBytes(StandardCharsets.UTF_8);
            String encodedMessage = new String(stringByte, StandardCharsets.UTF_8);

            // spilts message into two
            String[] content = encodedMessage.split(" ", 2);

            if (!(content[1].isEmpty())) {

                // Calls weather api
                String rawLocation = content[1];
                String location = URLEncoder.encode(rawLocation, StandardCharsets.UTF_8);

                String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" + location + "&appid=" + apikey + "&lang=de&units=metric";

                // main, unmodified, json object being fetched and decoded from the server.
                JsonObject root;

                try {
                    root = JSONHandler.getJSON(urlString);
                    if (root == null){
                        throw new NullPointerException("Rootobjekt ist unerwarteterweise null.");
                    }
                } catch (Exception e) {
                    event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    return;
                }

                // Param decoding starts here
                JsonArray weatherArray = root.getAsJsonArray("weather");

                if (weatherArray.size() == 0){
                    event.getChannel().sendMessage("Fehler mit Nachricht: API: Keine Stadt mit angegebenen Parametern gefunden.");
                    return;
                }

                JsonObject weatherActual;
                try{
                   weatherActual = weatherArray.get(0).getAsJsonObject();
                }catch(IndexOutOfBoundsException | IllegalStateException e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Kein valides Wetterarray in JSON gefunden.");
                    return;
                }

                JsonObject main = root.getAsJsonObject("main");

                String description = weatherActual.get("description").toString().replace("\"", "");
                String temperature = main.get("temp").toString();
                String city = root.get("name").toString();
                String image = weatherActual.get("icon").toString().replace("\"", "");

                int temp;

                try{

                    temp =  Double.valueOf(temperature).intValue();

                    if (description.isEmpty()){
                        throw new IllegalArgumentException("JSON: Wetterlage konnte nicht korrekt ausgelesen werden.");
                    }else if (city.isEmpty()){
                        throw new IllegalArgumentException("JSON: Stadt konnte nicht korrekt ausgelesen werden.");
                    }else if (image.isEmpty()){
                        throw new IllegalArgumentException("JSON: Bild-URL konnte nicht korrekt ausgelesen werden.");
                    }

                }catch (NumberFormatException e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Gradzahl kann nicht angezeigt werden");
                    return;
                }catch (IllegalArgumentException e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    return;
                }
                // Create EmbedBuilder with params
                EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor("OpenWeatherMap")
                        .setTitle(("Wetter f\u00fcr " + city))
                        .setDescription("Jetzt: " + temp + " Grad Celsius. Aktuelle Wetterlage: " + description + ".")
                        .setColor(Color.yellow)
                        .setThumbnail("https://openweathermap.org/img/wn/" + image + "@2x.png");
                // Send embed
                event.getChannel().sendMessage(embedBuilder);
            }else{
                event.getChannel().sendMessage("Fehler mit Nachricht: Das hat nicht geklappt. (Keine Ortsangabe gefunden)");
            }
        }
    }
}
