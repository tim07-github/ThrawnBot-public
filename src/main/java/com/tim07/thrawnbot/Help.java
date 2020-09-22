package com.tim07.thrawnbot;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;

/**
 * A help file, which lists all official functions of the bot to the server
 * @author u/tim07
 */

public class Help implements MessageCreateListener{
    /**
     * Listener on message being pushed into server channel
     * @param event message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        Message message = event.getMessage();
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.RED);

        switch (message.getContent()) {
            case "%help":
                // general help message
                embedBuilder.setDescription("Folgende Befehle gibt es: \n%wetter [Stadt] \n%wetterbericht [Stadt] [Anzahl der Tage] \n%thrawn \n%astro {marswetter} \n%affirmation \n%wuff \n%miau \n\nF\u00fcr weitere Infos %help [Befehl] in den Chat eingeben.");
                break;
                // custom help messages start here
            case "%help wetter":
                embedBuilder.setDescription("Befehl: %wetter [Stadt]\nGibt einen Wetterbericht zum jetzigen Zeitpunkt des gew\u00e4hlten Ortes aus.");
                break;
            case "%help wetterbericht":
                embedBuilder.setDescription("Befehl: %wetterbericht [Stadt] [Anzahl der Tage]\nGibt einen Tagesbericht des Wetters f\u00fcr den gew\u00e4hlten Ort aus. Die Anzahl ist frei bestimmbar, wird aber von der API begrenzt.");
                break;
            case "%help astro":
                embedBuilder.setDescription("Befehl: %astro {marswetter}\nT\u00e4glich aktualisierendes Astro-Bild der NASA oder das Wetter einzelner Marsmessstationen.");
                break;
            case "%help thrawn":
                embedBuilder.setDescription("Befehl: %thrawn\nSendet ein Zitat Thrawns (kleine Auswahl)");
                break;
            case "%help affirmation":
                embedBuilder.setDescription("Befehl: %affirmation\nWenn du mal aufmunternde Worte brauchst _schaut auf Tim_");
                break;
            case "%help miau":
                embedBuilder.setDescription("Befehl: %miau\nEin Katzenfoto! :tada:");
                break;
            case "%help wuff":
                embedBuilder.setDescription("Befehl: %wuff\nEin Hundfoto! :tada:");
                break;
            default:
                return;
        }
        //Send EmbedBuilder
        event.getChannel().sendMessage(embedBuilder);
    }
}
