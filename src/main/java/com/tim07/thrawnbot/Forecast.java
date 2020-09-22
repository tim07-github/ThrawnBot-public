package com.tim07.thrawnbot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;


import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * The forecast file shall post the weather forecast of to be determined dates
 * @author u/tim07
 * @see EmbedBuilder
 */

public class Forecast implements MessageCreateListener{

    /**
     * Listener on message being pushed into the server channel
     * @param event message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();

        // Checks if message belongs here
        if (message.getContent().startsWith("%wetterbericht ") && !message.getContent().equalsIgnoreCase("%wetterbericht ")){

            // Splits content into two
            String[] content = message.getContent().split(" ", 2);

            if (!(content[1].isEmpty())) {

                // determine dates to be shown
                String rawContent = content[1];
                String[] numberArray = rawContent.split(" ");
                String numberString = numberArray[numberArray.length - 1];
                int index;

                try{
                    index = Integer.parseInt(numberString);
                    if (index < 0){
                        throw new IllegalArgumentException("Eingabe: Negative Nummer eingegeben");
                    }
                }catch (Exception e){
                    if (e.getMessage().equals("Eingabe: Negative Nummer eingegeben")){
                        event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    }else{
                        event.getChannel().sendMessage("Fehler mit Nachricht: Eingabe: Keine Nummer eingegeben");
                    }
                    return;
                }

                String rawLocation = rawContent.substring(0, (rawContent.length() - numberString.length() - 1));
                String location = URLEncoder.encode(rawLocation, StandardCharsets.UTF_8);

                //fetch Coordinates for api crawl
                String longitude;
                String latitude;
                String city;
                JsonObject rootCoord;

                // 1st api call - needed for coordinates!
                try{
                    String urlString = "https://api.openweathermap.org/data/2.5/find?q=" + location + "&appid=APIKEY&lang=de&units=metric";
                    rootCoord = JSONHandler.getJSON(urlString);
                }catch (Exception e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    return;
                }

                //Coord
                JsonArray mainArray = rootCoord.getAsJsonArray("list");
                if (mainArray.size() == 0){
                    event.getChannel().sendMessage("Fehler mit Nachricht: API: Keine Stadt mit angegebenen Parametern gefunden.");
                    return;
                }
                JsonObject coord = mainArray.get(0).getAsJsonObject().getAsJsonObject("coord");
                longitude = coord.get("lon").getAsString();
                latitude = coord.get("lat").getAsString();

                //city
                city = mainArray.get(0).getAsJsonObject().get("name").toString().replaceAll("\"", "");

                try{
                    if (latitude.isEmpty() || longitude.isEmpty() || city.isEmpty()){
                        throw new IllegalArgumentException("API: Parameter für Onecall API Abfrage konnten nicht richtig ausgelesen werden.");
                    }
                }catch(IllegalArgumentException e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    return;
                }

                JsonObject root;

                //fetch actual weather api;
                try {
                    String urlString = "https://api.openweathermap.org/data/2.5/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=hourly,minutely,current&appid=APIKEY&lang=de&units=metric";

                    // JSON can be used now
                    root = JSONHandler.getJSON(urlString);
                    if (root == null){
                        throw new NullPointerException("JSOM: Rootobjekt ist unerwarteterweise null");
                    }

                } catch (Exception e) {
                    event.getChannel().sendMessage("Fehler mit Nachricht: " + e.getMessage());
                    return;
                }

                // JSON parsing begins here

                long timezone_offset;
                try {
                    timezone_offset = root.get("timezone_offset").getAsLong();
                }catch(Exception e){
                    event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Zeitzonen Offset konnte nicht ausgelesen werden");
                    return;
                }

                JsonArray dailyArray = root.getAsJsonArray("daily");

                // index = days to be shown - limited by 8 day forecast :)
                for (int i = 0; (i < index && i < dailyArray.size()); i++){
                    JsonObject e = dailyArray.get(i).getAsJsonObject();

                    JsonObject daily = e.getAsJsonObject();
                    try {
                        //Get date;
                        long time = daily.get("dt").getAsLong();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        LocalDateTime date = LocalDateTime.ofEpochSecond(time + timezone_offset, 0, ZoneOffset.UTC);
                        String outputDate = date.format(formatter);

                        //Get Temperature
                        JsonObject temp = daily.getAsJsonObject("temp");
                        int maxTemp = temp.get("max").getAsInt();
                        int minTemp = temp.get("min").getAsInt();

                        JsonArray weatherArray = daily.getAsJsonArray("weather");
                        JsonObject weather = weatherArray.get(0).getAsJsonObject();

                        String description = weather.get("description").toString().replaceAll("\"", "");
                        String icon = weather.get("icon").toString().replaceAll("\"", "");
                        // Create EmbedBuilder with params
                        EmbedBuilder embedBuilder = new EmbedBuilder()
                                .setTitle("Wetter vom " + outputDate + " f\u00fcr: " + city)
                                .setAuthor("OpenWeatherMaps")
                                .setThumbnail("https://openweathermap.org/img/wn/" + icon + "@2x.png")
                                .addField("Wetterlage", description)
                                .addInlineField("Minimalste Temperatur", String.valueOf(minTemp))
                                .addInlineField("Maximalste Temperatur", String.valueOf(maxTemp))
                                .setColor(Color.BLUE);
                        // Send message
                        event.getChannel().sendMessage(embedBuilder);
                    }catch(Exception exception){
                        event.getChannel().sendMessage("Fehler mit Nachricht: JSON: Beim Auslesen der JSON ist ein Fehler aufgetreten");
                        return;
                    }
                }
            }else{
                event.getChannel().sendMessage("Fehler mit Nachricht: Das hat nicht geklappt. (Keine Ortsangabe gefunden)");
            }
        }
    }
}
