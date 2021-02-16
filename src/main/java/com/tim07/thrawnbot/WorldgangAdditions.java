package com.tim07.thrawnbot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Simple Additions for the Worldgang Server
 */
public class WorldgangAdditions extends AbstractMessageCreateListener {

    Map<String, Integer> statistic = new HashMap<>();

    public WorldgangAdditions() {
        loadData();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        super.onMessageCreate(messageCreateEvent);
        if (super.blacklisted) {
            return;
        }
        if (!messageCreateEvent.getServer().get().getName().equals("worldgang")) {
            return;
        }
        if (messageCreateEvent.getMessageContent().contains("https://tenor.com/view/triggered-angry-mad-gif-12026482") ||
                messageCreateEvent.getMessageContent().contains("https://tenor.com/YCNG.gif")) {
            parseMessage(messageCreateEvent);
        } else if (messageCreateEvent.getMessageContent().equalsIgnoreCase("%triggered")) {
            output(messageCreateEvent);
        }
    }

    /**
     * Checks message for specific GIF and updates {@link Map}
     *
     * @param event {@link MessageCreateEvent} requesting message
     */
    private void parseMessage(MessageCreateEvent event) {
        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        for (User user : mentionedUsers) {
            if (statistic.containsKey(user.getDiscriminatedName())) {
                statistic.replace(user.getDiscriminatedName(), statistic.get(user.getDiscriminatedName()) + 1);
            } else {
                statistic.put(user.getDiscriminatedName(), 1);
            }
        }
        event.addReactionsToMessage("\u2705");
        storeData();
    }

    /**
     * Prints out the {@link Map} in a more visual way
     *
     * @param event {@link MessageCreateEvent} requesting message
     */
    private void output(MessageCreateEvent event) {
        StringBuilder output = new StringBuilder();
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(statistic.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Integer> entry : sorted) {
            output.append("**").append(entry.getKey()).append("**: ").append(entry.getValue()).append("\n");
        }
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Triggered-Ranking")
                .setDescription(output.toString())
                .setColor(Color.red);
        event.getChannel().sendMessage(embedBuilder);
    }

    /**
     * loads stored {@link Map} data
     */
    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream i = new ObjectInputStream(new FileInputStream("triggered.ser"))) {
            statistic = Collections.unmodifiableMap((Map<String, Integer>) i.readObject());
        } catch (IOException | ClassNotFoundException e) {
            statistic = new HashMap<>();
        }
    }

    /**
     * Stores linked {@link Map} data
     */
    private void storeData() {
        try (ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream("triggered.ser"))) {
            o.writeObject(statistic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
