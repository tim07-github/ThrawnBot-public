package com.tim07.thrawnbot;


import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.emoji.CustomEmoji;
import org.javacord.api.entity.message.Reaction;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class shall provide us some insight data regarding our server/channel usage
 *
 * @author u/tim07
 */
public class ChatStats extends AbstractMessageCreateListener {

    /**
     * This method is called every time a message is created.
     *
     * @param event The event.
     */
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        super.onMessageCreate(event);
        if (blacklisted){
            return;
        }

        if (event.getMessageContent().startsWith("%chatstats")) {
            chatstats(event);
        } else if (event.getMessageContent().equalsIgnoreCase("%userstats")) {
            userstats(event);
        }else if (event.getMessageContent().equalsIgnoreCase("%emojistats")){
            emojistats(event);
        }else if (event.getMessageContent().equalsIgnoreCase("%reactionstats")) {
            reactionstats(event);
        }else if (event.getMessageContent().equalsIgnoreCase("%rolestats")) {
            rolestats(event);
        }
    }

    /**
     * Sends out the user statistics per server.
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void userstats(MessageCreateEvent event){

        Server server;
        if (event.getServer().isPresent()) {
            server = event.getServer().get();
        } else {
            return;
        }

        List<ServerTextChannel> channels = server.getTextChannels();
        if (server.getMemberByDiscriminatedName("ThrawnBot#2810").isEmpty()){
            return;
        }
        int visibleTextChannels = (int) channels.stream()
                .filter(channel -> channel
                        .canSee(server.getMemberByDiscriminatedName("ThrawnBot#2810").get()))
                .count();

        Map<ServerTextChannel, Integer> channelstats = new HashMap<>();
        Map<String, Integer> userstats = new HashMap<>();


        for (ServerTextChannel channel : server.getTextChannels()) {
            // Async call to API -> Fetch data and process it
            channel.getMessages(Integer.MAX_VALUE).thenAccept(messages -> {
                channelstats.put(channel, messages.size());
                messages.forEach(message -> {
                    String author = message.getAuthor().getName();
                    if (userstats.containsKey(author)) {
                        userstats.replace(author, userstats.get(author) + 1);
                    } else {
                        userstats.put(author, 1);
                    }
                });
            }).thenAccept(e -> {
                // Sort and publish data when all channels have been fetched and processed
                if (channelstats.size() == visibleTextChannels) {
                    List<Map.Entry<ServerTextChannel, Integer>> sortedChannelList = new ArrayList<>(channelstats.entrySet());
                    sortedChannelList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    List<Map.Entry<String, Integer>> sortedUserList = new ArrayList<>(userstats.entrySet());
                    sortedUserList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    StringBuilder topUsers = new StringBuilder();
                    for (Map.Entry<String, Integer> entry : sortedUserList) {
                        if (topUsers.length() > 1023) {
                            topUsers.delete(1023, topUsers.length());
                        }
                        if (topUsers.length() > 1000) {
                            continue;
                        }
                        String user = entry.getKey();
                        int count = entry.getValue();
                        topUsers.append("**").append(user).append("**").append(": ").append(count).append("\n");
                    }

                    StringBuilder topChannels = new StringBuilder();
                    for (Map.Entry<ServerTextChannel, Integer> entry : sortedChannelList) {
                        if (topChannels.length() > 1023) {
                            topChannels.delete(1023, topChannels.length());
                        }
                        if (topChannels.length() > 1000) {
                            continue;
                        }
                        ServerTextChannel textChannel = entry.getKey();
                        String name = textChannel.getName();
                        int count = entry.getValue();
                        topChannels.append("**").append(name).append("**").append(": ").append(count).append("\n");
                    }


                    int messagesServer = channelstats.values().stream().mapToInt(Integer::intValue).sum();

                    double giniCoefficient = calculateGini(userstats.values());
                    double giniChannel = calculateGini(channelstats.values());

                    EmbedBuilder messageBuilder = new EmbedBuilder()
                            .setTitle("Stats f\u00FCr " + server.getName())
                            .addField("**Nutzer** (Absteigend sortiert) ", topUsers.toString())
                            .addField("**Gesamtanzahl der Nutzer mit Nachrichten**", String.valueOf(sortedUserList.size()))
                            .addField("**Channel-Nachrichten**:", topChannels.toString())
                            .addField("**Gesamtnachrichten im Server**", String.valueOf(messagesServer))
                            .addField("**Gini-Koeffizient der Nutzer nach Nachrichten**", String.format("%.2f", giniCoefficient))
                            .addField("**Gini-Koeffizient der Channels nach Nachrichten**", String.format("%.2f", giniChannel))
                            .setColor(Color.CYAN);
                    event.getChannel().sendMessage(messageBuilder);
                }
            });
        }
    }

    /**
     * Sends out the channels statistics in a given timeframe [in Messages]
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void chatstats(MessageCreateEvent event){
        int limit = 0;
        try {
            String limitString = event.getMessageContent().split(" ", 2)[1];
            limit = Integer.parseInt(limitString);
        } catch (Exception e) {
            event.getChannel().sendMessage(
                    "Fehler: ChatChart konnte leider nicht erstellt werden: Nummer nicht auslesbar.");
            return;
        }
        Map<String, Integer> valueMap = new HashMap<>();

        event.getChannel().getMessages(limit).thenAccept(messages -> {
            messages.forEach(message -> {
                String author = message.getAuthor().getName();
                if (valueMap.containsKey(author)) {
                    valueMap.put(author, valueMap.get(author) + 1);
                } else {
                    valueMap.put(author, 1);
                }
            });

            List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(valueMap.entrySet());
            sortedList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            StringBuilder topUsers = new StringBuilder();
            for (Map.Entry<String, Integer> entry : sortedList) {
                String user = entry.getKey();
                int count = entry.getValue();
                topUsers.append(user).append(": ").append(count).append("\n");
            }

            double giniCoefficient = calculateGini(valueMap.values());
            String name;
            if (event.getServer().isPresent()){
                name = event.getServer().get().getName();
            }else{
                name = "Unspezifiziert";
            }
            EmbedBuilder messageBuilder = new EmbedBuilder()
                    .setTitle("Stats f\u00FCr " + name)
                    .addField("Nutzer (Absteigend sortiert) ", topUsers.toString())
                    .addField("Gesamtanzahl der schreibenden Nutzer", String.valueOf(sortedList.size()))
                    .addField("Gesamtnachrichten im Channel", String.valueOf(messages.size()))
                    .addField("**Gini-Koeffizient der Nutzer nach Nachrichten im Kanal**", String.format("%.2f", giniCoefficient))
                    .setColor(Color.CYAN);
            event.getChannel().sendMessage(messageBuilder);
        });

    }

    /**
     * Sends out the emojis statistics in a given server.
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void  emojistats(MessageCreateEvent event){
        Server server;
        if (event.getServer().isPresent()) {
            server = event.getServer().get();
        } else {
            return;
        }
        List<ServerTextChannel> channels = server.getTextChannels();
        List<ServerTextChannel> visibleChannels = channels.stream()
                .filter(channel -> channel
                        .canSee(server.getMemberByDiscriminatedName("ThrawnBot#2810").get())).collect(Collectors.toList());
        int visibleTextChannels = visibleChannels.size();
        Map<String, Integer> emojimap = new HashMap<>();
        List<ServerTextChannel> channelList = new ArrayList<>();

        for (ServerTextChannel channel : visibleChannels) {
            // Async call to API -> Fetch data and process it
            channel.getMessages(Integer.MAX_VALUE).thenAccept(messages -> {
                messages.forEach(message -> {
                    List<CustomEmoji> emojis = message.getCustomEmojis();
                    emojis.forEach(e -> {
                        String name = e.getName();
                        if (emojimap.containsKey(name)){
                            int i = emojimap.get(name);
                            emojimap.replace(name, i+1);
                        }else{
                            emojimap.put(name, 1);
                        }
                    });
                });
                channelList.add(channel);
            }).thenAccept(unused -> {
                if (channelList.size() == visibleTextChannels){

                    List<Map.Entry<String, Integer>> mapList = new ArrayList<>(emojimap.entrySet());
                    mapList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    StringBuilder topEmojis = new StringBuilder();
                    if (emojimap.isEmpty()){
                        topEmojis.append("**Keine Emojis auf dem Server gepostet**");
                    }
                    for (Map.Entry<String, Integer> entry : mapList) {
                        if (topEmojis.length() > 1023) {
                            topEmojis.delete(1023, topEmojis.length());
                        }
                        if (topEmojis.length() > 1000) {
                            continue;
                        }
                        String emoji = entry.getKey();
                        int count = entry.getValue();
                        topEmojis.append("**").append(emoji).append("**").append(": ").append(count).append("\n");
                    }
                    double giniEmoji = calculateGini(emojimap.values());

                    EmbedBuilder messageBuilder = new EmbedBuilder()
                            .setTitle("Stats f\u00FCr " + server.getName())
                            .addField("**Emojiranking**", topEmojis.toString())
                            .addField("**Gini-Koeffizient nach Emojinutzung**", String.format("%.2f", giniEmoji))
                            .setColor(Color.CYAN);
                    event.getChannel().sendMessage(messageBuilder);
                }
            });
        }
    }

    /**
     * Sends out the reaction statistics of a given Server.
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void reactionstats(MessageCreateEvent event) {
        Server server;
        if (event.getServer().isPresent()) {
            server = event.getServer().get();
        } else {
            return;
        }
        List<ServerTextChannel> channels = server.getTextChannels();
        List<ServerTextChannel> visibleChannels = channels.stream()
                .filter(channel -> channel
                        .canSee(server.getMemberByDiscriminatedName("ThrawnBot#2810").get())).collect(Collectors.toList());
        int visibleTextChannels = visibleChannels.size();
        Map<String, Integer> emojimap = new HashMap<>();
        List<ServerTextChannel> channelList = new ArrayList<>();

        for (ServerTextChannel channel : visibleChannels) {
            // Async call to API -> Fetch data and process it
            channel.getMessages(Integer.MAX_VALUE).thenAccept(messages -> {
                messages.forEach(message -> {
                    List<Reaction> reactions = message.getReactions();
                    reactions.forEach(reaction -> {
                        if (!reaction.getEmoji().isUnicodeEmoji()) {
                            String emoji = reaction.getEmoji().asCustomEmoji().get().getName();
                            if (emojimap.containsKey(emoji)) {
                                int i = emojimap.get(emoji);
                                emojimap.replace(emoji, i + reaction.getCount());
                            } else {
                                emojimap.put(emoji, reaction.getCount());
                            }
                        } else {
                            String emoji = reaction.getEmoji().asUnicodeEmoji().get();
                            if (emojimap.containsKey(emoji)) {
                                int i = emojimap.get(emoji);
                                emojimap.replace(emoji, i + reaction.getCount());
                            } else {
                                emojimap.put(emoji, reaction.getCount());
                            }
                        }
                    });
                });
                channelList.add(channel);
            }).thenAccept(unused -> {
                if (channelList.size() == visibleTextChannels) {

                    List<Map.Entry<String, Integer>> mapList = new ArrayList<>(emojimap.entrySet());
                    mapList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

                    StringBuilder topEmojis = new StringBuilder();
                    if (emojimap.isEmpty()) {
                        topEmojis.append("**Keine Emojis auf dem Server gepostet**");
                    }
                    for (Map.Entry<String, Integer> entry : mapList) {
                        if (topEmojis.length() > 1023) {
                            topEmojis.delete(1023, topEmojis.length());
                        }
                        if (topEmojis.length() > 1000) {
                            continue;
                        }
                        String emoji = entry.getKey();
                        int count = entry.getValue();
                        topEmojis.append("**").append(emoji).append("**").append(": ").append(count).append("\n");
                    }
                    double giniEmoji;

                    try {
                        double[] data = new double[emojimap.values().size()];
                        int i = 0;
                        for (int v : emojimap.values()) {
                            data[i] = v;
                            i++;
                        }
                        giniEmoji = gini(data);
                    } catch (Exception ex) {
                        giniEmoji = Double.NaN;
                    }

                    EmbedBuilder messageBuilder = new EmbedBuilder()
                            .setTitle("Stats f\u00FCr " + server.getName())
                            .addField("**Emojiranking (Reaktionen)**", topEmojis.toString())
                            .addField("**Gini-Koeffizient nach Emojinutzung**", String.format("%.2f", giniEmoji))
                            .setColor(Color.CYAN);
                    event.getChannel().sendMessage(messageBuilder);
                }
            });
        }
    }

    /**
     * Sends out the role statistics of a given server.
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void rolestats(MessageCreateEvent event){
        HashMap<Role, Integer> roleMap = new HashMap<>();

        if (event.getServer().isPresent()) {
            for (Role role : event.getServer().get().getRoles()) {
                int roleCount = role.getUsers().size();
                roleMap.put(role, roleCount);
            }
        }

        List<Map.Entry<Role, Integer>> sortedChannelList = new ArrayList<>(roleMap.entrySet());
        sortedChannelList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        StringBuilder roleCountString = new StringBuilder();
        for (Map.Entry<Role, Integer> entry : sortedChannelList){

            if (roleCountString.length() > 1023) {
                roleCountString.delete(1023, roleCountString.length());
            }
            if (roleCountString.length() > 1000) {
                continue;
            }
            String name = entry.getKey().getName().replaceFirst("@", "");
            int value = entry.getValue();
            roleCountString.append("**").append(name).append("**: ").append(String.valueOf(value)).append("\n");
        }
        double giniRoles;

        try {
            double[] data = new double[roleMap.values().size()];
            int i = 0;
            for (int v : roleMap.values()) {
                data[i] = v;
                i++;
            }
            giniRoles = gini(data);
        } catch (Exception ex) {
            giniRoles = Double.NaN;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Stats f\u00FCr " + event.getServer().get().getName())
                .addField("**Rollenranking**", roleCountString.toString())
                .addField("**Gini-Koeffizient nach Rollennutzung**", String.format("%.2f", giniRoles))
                .setColor(Color.CYAN);
        event.getChannel().sendMessage(embedBuilder);
    }

    /**
     * Gini-Koeffizientenberechung nach Wikipedia
     * (von Chris-Goldapp)
     * @param data Array mit Nummern
     * @return Gini-Koeffizient
     */
    double gini(double[] data) {
        if (data == null || data.length == 0) {
            return Double.NaN;
        }
        Arrays.sort(data);
        int n = data.length;
        double avg = Arrays.stream(data).sum() / n;

        double sigma = 0.0;

        for (int i = 1; i <= n; i++) {
            sigma += i * (data[i - 1] - avg);
        }

        return (2.0 / (n * n * avg)) * sigma;
    }

    /**
     * Umwandlung der Statistiken von Discord in auslesbares Array und Giniberechnung
     * @param values Nachrichtenanzahl als Collection
     * @return Ginikoeffizient
     */
    double calculateGini(Collection<Integer> values){
        try {
            double[] data = new double[values.size()];
            int i = 0;
            for (int v : values) {
                data[i] = v;
                i++;
            }
            return gini(data);
        } catch (Exception ex) {
            return Double.NaN;
        }
    }
}
