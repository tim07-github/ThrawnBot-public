package com.tim07.thrawnbot;


import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

/**
 * A help file, which lists all official functions of the bot to the server
 * @author u/tim07
 */

public class Help extends AbstractMessageCreateListener{
    /**
     * Listener on message being pushed into server channel
     * @param event message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
       super.onMessageCreate(event);
       if (super.blacklisted){
           return;
       }
       EmbedBuilder embedBuilder = new EmbedBuilder().setColor(Color.RED);

        switch (event.getMessageContent()) {
            case "%help":
                // general help message
                embedBuilder.setDescription("Folgende Befehle gibt es: \n%wetter [Stadt] \n%wetterbericht [Stadt] [Anzahl der Tage] \n%thrawn \n%astro \n%affirmation \n%wuff \n%miau \n%nachrichten \n%play \n%chatstats [Anzahl der Nachrichten] \n%userstats \n\nF\u00fcr weitere Infos %help [Befehl] in den Chat eingeben.");
                break;
                // custom help messages start here
            case "%help wetter":
                embedBuilder.setDescription("Befehl: %wetter [Stadt]\nGibt einen Wetterbericht zum jetzigen Zeitpunkt des gew\u00e4hlten Ortes aus.");
                break;
            case "%help wetterbericht":
                embedBuilder.setDescription("Befehl: %wetterbericht [Stadt] [Anzahl der Tage]\nGibt einen Tagesbericht des Wetters f\u00fcr den gew\u00e4hlten Ort aus. Die Anzahl ist frei bestimmbar, wird aber von der API begrenzt.");
                break;
            case "%help astro":
                embedBuilder.setDescription("Befehl: %astro \nT\u00e4glich aktualisierendes Astro-Bild der NASA");
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
                embedBuilder.setDescription("Befehl: %wuff\nEin Hundefoto! :tada:");
                break;
            case "%help nachrichten":
                embedBuilder.setDescription("Befehl: %nachrichten\nBBCBweaking wird dich über das Neueste informieren");
                break;
            case "%help play":
                embedBuilder.setDescription("Befehl: %play [yt-link]\nDer Bot joint dich im Voice und spielt das gewünschte Video vor.");
                break;
            case "%help chatstats":
                embedBuilder.setDescription("Befehl: %chatstats [Anzahl der Nachrichten]\nAuswertungen aller Nachrichten im geschriebenen Kanal");
                break;
            case "%help userstats":
                embedBuilder.setDescription("Befehl: %userstats\nAuswertungen aller Nachrichten im Server");
                break;
            default:
                return;
        }
        //Send EmbedBuilder
        event.getChannel().sendMessage(embedBuilder);
    }
}
