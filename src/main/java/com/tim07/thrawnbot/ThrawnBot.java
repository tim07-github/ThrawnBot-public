package com.tim07.thrawnbot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.user.UserStatus;


import java.util.*;

/**
 * Main file of "ThrawnBot", a discord bot created by u/tim07.
 * This file activates all the other listeners available in the com.mischok.thrawnbot package and implements some instant reactions.
 * @author u/tim07
 * @version 2.0
 */
public class ThrawnBot {

    protected static final List<User> blacklistedUsers = new ArrayList<>();
    protected static Properties property = new Properties();

    protected static Properties createProperty(){
        try {
            Properties properties = new Properties();
            properties.load(ThrawnBot.class.getClassLoader().getResourceAsStream("META-INF/gradle.properties"));
            return properties;
        }catch (Exception e){
           e.printStackTrace();
           System.exit(1);
        }
        return new Properties();
    }

    /**
     * The main method can be called to activate the entire bot, so please be careful if you do :)
     * @param args f.ex. the bot token can be given into the main function with this parameter.
     */

    public static void main(String[] args) {
        property = createProperty();
        // Please use another token, when connecting to the discord api. The token *will* be removed in future updates.
        String token = (String) property.get("API_KEY_DISCORD");

        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login()
                .join();
        api.updateStatus(UserStatus.ONLINE);
        api.updateActivity(ActivityType.PLAYING,"Seventh fleet");
        api.setMessageCacheSize(10, 60*60);



        api.addMessageCreateListener( event -> {
           if (event.getMessageContent().startsWith("%override add") && event.getMessageAuthor().isBotOwner()){
               List<User> blacklist = event.getMessage().getMentionedUsers();
               blacklistedUsers.addAll(blacklist);
           }else if (event.getMessageContent().startsWith("%override remove") && event.getMessageAuthor().isBotOwner()){
               List<User> blacklist = event.getMessage().getMentionedUsers();
               blacklistedUsers.removeAll(blacklist);
           }
        });
        // Add a listener which answers with "Pong!" if someone writes "ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
               return;
            }
            if (event.getMessageContent().equalsIgnoreCase("%ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });

        // Adds a listener which replies to "bad (working) bot" in a chippy way.
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().contains("Schlechter Bot") || event.getMessageContent().contains("schlechter Bot") || event.getMessageContent().contains("schlechter bot") || event.getMessageContent().contains("Schlechter bot")){
                String message = "Ich bin eh besser als Julian :wink:\n_Bitte nicht hauen_";
                event.getChannel().sendMessage(message);
            }
        });

        // Adds a listener which replies to "Execute Order" and the german equivalent: "it will be done, my lord".
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().startsWith("Führt die Order") || event.getMessageContent().startsWith("Execute Order") || event.getMessageContent().startsWith("execute order") || event.getMessageContent().startsWith("führt die Order")){
                event.getChannel().sendMessage("It will be done, my lord.");
            }
        });

        // Adds a listener which posts a gif link when the user writes "that's what she said"
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().equalsIgnoreCase("das ist, was sie sagte") || event.getMessageContent().equalsIgnoreCase("that's what she said")){
                event.deleteMessage();
                event.getChannel().sendMessage("https://media.giphy.com/media/ToMjGpMhVjTvjX5nLs4/giphy.gif");
            }
        });

        // Adds a shutdown listener - Only accessible from the bot-owner
        api.addMessageCreateListener( event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().equalsIgnoreCase("%shutdown") && event.getMessageAuthor().isBotOwner()){
                event.getChannel().sendMessage("Daisy, Daisy...");
                api.updateStatus(UserStatus.OFFLINE);
                System.exit(0);
            }
        });

        // Adds a listener to show gratitude towards those who are thankful
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().equalsIgnoreCase("guter bot")){
                event.getMessage().addReaction("\uD83E\uDD70");
            }
        });

        //observer object, which is used to gather the AuditLogEntries used to determine if a comment has been removed by mods
        var observer = new Object() {

            /**
             * observers stores AuditLogs of the relevant servers
             */
            private final Map<Server, List<AuditLogEntry>> observers = new HashMap<>();

            /**
             * getObservedList will return an empty list, when the mods have not yet deleted comments of others, or will return the last read AuditLogEntry list, which can be used for comparison with the one being present.
             * @param key Server in which the removal action has been issued
             * @return AuditLogEntry list, which has been stored the last time updateEntry has been called. It is therefore a good reference to call when looking for comment removals.
             */
            public List<AuditLogEntry> getObservedList(Server key){
                return observers.get(key);
            }

            /**
             * addObserver will create an empty map-entry for the newly discovered server
             * @param server Server in which the removal action has been issued
             */
            public void addObserver(Server server){
                if (!observers.containsKey(server)){
                    observers.put(server, new ArrayList<>());
                }
            }

            /**
             * isPresent checks for the server being known to the bot
             * @param server Server in which the removal action has been issued
             * @return true, if the server is known. Otherwise false.
             */
            public boolean isPresent(Server server){
                return observers.containsKey(server);
            }

            /**
             * updateEntry updates observers according to the provided parameters
             * @param server Server in which the removal action has been issued
             * @param entryList Logs which have been fetched from the server above
             */
            public void updateEntry(Server server, List<AuditLogEntry> entryList){
                observers.put(server, entryList);
            }
        };

        // Adds a listener to determine if a user-comment has been removed by mods and if so, cry out loud.
        api.addMessageDeleteListener(event -> {
            Optional<Server> server = event.getServer();
            server.ifPresent(value -> value.getAuditLog(10, AuditLogActionType.MESSAGE_DELETE).thenAcceptAsync(auditLog -> {
                if (!observer.isPresent(value)){
                    observer.addObserver(value);
                }
                List<AuditLogEntry> auditLogEntries = auditLog.getEntries();
                List<AuditLogEntry> savedEntries = observer.getObservedList(value);

                if (savedEntries.isEmpty()) {
                    observer.updateEntry(value, auditLogEntries);
                }else if (!auditLogEntries.equals(savedEntries)){
                    observer.updateEntry(value, auditLogEntries);
                    event.getChannel().sendMessage("ZENSUR!");
                }
            }));
        });

        // Adds a listener to perform a "Simon says" action while triggered by the bot owner.
        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageAuthor().isBotOwner() && event.getMessageContent().startsWith("%say ")){
                String message = event.getMessageContent().split(" ", 2)[1];
                event.deleteMessage();
                event.getChannel().sendMessage(message);
            }
        });

        Music music = new Music(api);
        api.addServerVoiceChannelMemberJoinListener(event -> {
            if(event.getUser().isBotOwner()){
                event.getChannel().connect().thenAccept(audioConnection -> {
                    music.setAudioConnectionStandard(audioConnection);
                    music.play("https://youtu.be/xlE8qkogG4g");
                });
            }
        });
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equals("%disconnect") && event.getMessageAuthor().isBotOwner()){
                music.disconnect();
            }
        });

        api.addMessageCreateListener( event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
            if (event.getMessageContent().contains("This is the way") && !event.getMessageAuthor().isYourself()){
                event.getChannel().sendMessage("This is the way");
            }
        });

        api.addMessageCreateListener(event -> {
            if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().isWebhook()){
                return;
            }
            User user = event.getMessageAuthor().asUser().orElse(null);
            if (blacklistedUsers.contains(user)){
                return;
            }
           if (event.getMessageContent().startsWith("%play")){
               String message = event.getMessageContent().split( " ", 2)[1];
               Optional<ServerVoiceChannel> channel = event.getMessageAuthor().getConnectedVoiceChannel();
               if (channel.isPresent()){
                   if (channel.get().isConnected(api.getClientId())){
                       music.play(message);
                   }else{
                       channel.get().connect().thenAccept(audioConnection -> {
                           music.setAudioConnectionStandard(audioConnection);
                           music.play(message);
                       });
                   }
               }
           }
        });

        // Adds the listeners outsourced into own files
        api.addListener(new Thrawn());
        api.addListener(new Weather());
        api.addListener(new Forecast());
        api.addListener(new AstroPhotoOfTheDay());
        api.addListener(new Help());
        api.addListener(new Affirmation());
        api.addListener(new AnimalPic());
        api.addListener(new Twitter());
        api.addListener(new ChatStats());
        api.addListener(new PingSystem());
        api.addListener(new WorldgangAdditions());

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }
}
