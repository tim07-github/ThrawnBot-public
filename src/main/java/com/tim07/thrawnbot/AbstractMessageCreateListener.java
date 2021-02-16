package com.tim07.thrawnbot;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

/**
 * Abstract class implementing {@link MessageCreateListener} for simple administrative tasks, like block.
 */
public abstract class AbstractMessageCreateListener implements MessageCreateListener {
    boolean blacklisted = false;
    /**
     * Listener on message pushed into the server channel
     * @param messageCreateEvent message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if (messageCreateEvent.getMessageAuthor().isBotUser() ||
            messageCreateEvent.getMessageAuthor().isWebhook()){
            blacklisted = true;
        }
        User user = messageCreateEvent.getMessageAuthor().asUser().orElse(null);
        if (ThrawnBot.blacklistedUsers.contains(user)){
            blacklisted = true;
        }
        blacklisted = false;
    }
}
