package com.tim07.thrawnbot;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ChatStats implements MessageCreateListener {

    /**
     * This method is called every time a message is created.
     *
     * @param event The event.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().startsWith("%chatstats")){
            int limit = 0;
            try{
                String limitString = event.getMessageContent().split( " ", 2)[1];
                limit = Integer.parseInt(limitString);
            } catch (Exception e){
                event.getChannel().sendMessage(
                        "Fehler: ChatChart konnte leider nicht erstellt werden: Nummer nicht auslesbar.");
                return;
            }
            Map<String, Integer> valueMap = new HashMap<>();
            TextChannel textChannel = event.getChannel();

            textChannel.getMessages(limit).thenAccept(messages -> {
               messages.forEach(message -> {
                   String author = message.getAuthor().getName();
                    if (valueMap.containsKey(author)){
                        valueMap.put(author, valueMap.get(author) + 1);
                    }else{
                        valueMap.put(author, 1);
                    }
               });
               List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(valueMap.entrySet());
               sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

               StringBuilder topUsers  = new StringBuilder();
               for (Map.Entry<String, Integer> entry : sortedList){
                   String user = entry.getKey();
                   int count = entry.getValue();
                   topUsers.append(user).append(": ").append(count).append("\n");
               }

                EmbedBuilder messageBuilder = new EmbedBuilder()
                        .setTitle("Stats f\u00FCr " + event.getServerTextChannel().get().getName())
                        .addField("Nutzer (Absteigend sortiert) ", topUsers.toString())
                        .addField("Gesamtanzahl der schreibenden Nutzer", String.valueOf(sortedList.size()))
                        .addField("Gesamtnachrichten im Channel", String.valueOf(messages.size()))
                        .setColor(Color.CYAN);
                event.getChannel().sendMessage(messageBuilder);
            });


        } else if (event.getMessageContent().equalsIgnoreCase("%userstats")){
            Server server = event.getServer().get();
            List<ServerTextChannel> channels = server.getTextChannels();
            int visibleTextChannels = (int) channels.stream()
                    .filter(channel -> channel
                            .canSee(server.getMemberByDiscriminatedName("ThrawnBot#2810").get()))
                    .count();

            Map<ServerTextChannel, Integer> channelstats = new HashMap<>();
            Map<String, Integer> userstats = new HashMap<>();

            for (ServerTextChannel channel : server.getTextChannels()){
                channel.getMessages(Integer.MAX_VALUE).thenAccept(messages -> {
                    channelstats.put(channel, messages.size());
                    messages.forEach( message -> {
                        String author = message.getAuthor().getName();
                        if (userstats.containsKey(author)){
                            userstats.replace(author, userstats.get(author) + 1);
                        }else{
                            userstats.put(author, 1);
                        }
                    });
                }).thenAccept(e -> {

                    if (channelstats.size() == visibleTextChannels) {
                        List<Map.Entry<ServerTextChannel, Integer>> sortedChannelList = new ArrayList<>(channelstats.entrySet());
                        sortedChannelList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                        List<Map.Entry<String, Integer>> sortedUserList = new ArrayList<>(userstats.entrySet());
                        sortedUserList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                        StringBuilder topUsers  = new StringBuilder();
                        for (Map.Entry<String, Integer> entry : sortedUserList){
                            if (topUsers.length() > 1023){
                                topUsers.delete(1023, topUsers.length());
                            }
                            if (topUsers.length() > 1000){
                                continue;
                            }
                            String user = entry.getKey();
                            int count = entry.getValue();
                            topUsers.append("**").append(user).append("**").append(": ").append(count).append("\n");
                        }

                        StringBuilder topChannels  = new StringBuilder();
                        for (Map.Entry<ServerTextChannel, Integer> entry : sortedChannelList){
                            if (topChannels.length() > 1023){
                                topChannels.delete(1023, topChannels.length());
                            }
                            if (topChannels.length() > 1000){
                                continue;
                            }
                            ServerTextChannel textChannel = entry.getKey();
                            String name = textChannel.getName();
                            int count = entry.getValue();
                            topChannels.append("**").append(name).append("**").append(": ").append(count).append("\n");
                        }
                        int messagesServer = channelstats.values().stream().mapToInt(Integer::intValue).sum();



                        EmbedBuilder messageBuilder = new EmbedBuilder()
                                .setTitle("Stats f\u00FCr " + server.getName())
                                .addField("**Nutzer** (Absteigend sortiert) ", topUsers.toString())
                                .addField("**Gesamtanzahl der schreibenden Nutzer**", String.valueOf(sortedUserList.size()))
                                .addField("**Channelnachrichten**:", topChannels.toString())
                                .addField("**Gesamtnachrichten im Server**", String.valueOf(messagesServer))
                                .setColor(Color.CYAN);
                        event.getChannel().sendMessage(messageBuilder);
                    }
                });
            }
        }
    }
}
