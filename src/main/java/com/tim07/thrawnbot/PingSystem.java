package com.tim07.thrawnbot;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

/**
 * Simple commands to organize ping groups over {@link Role}
 */
public class PingSystem extends AbstractMessageCreateListener{

    /**
     * Listener on message pushed into the server channel
     * @param messageCreateEvent message with extra information
     */
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        super.onMessageCreate(messageCreateEvent);
        if (super.blacklisted){
            return;
        }
        if (messageCreateEvent.getMessageContent().equals("%ping")){
            return;
        }
        if (messageCreateEvent.getMessageContent().startsWith("%ping")){
            ping(messageCreateEvent);
        }else if (messageCreateEvent.getMessageContent().startsWith("%createping")){
            createPing(messageCreateEvent);
        }else if (messageCreateEvent.getMessageContent().startsWith("%addping")){
            addPing(messageCreateEvent);
        }else if (messageCreateEvent.getMessageContent().startsWith("%removeping")){
            removePings(messageCreateEvent);
        }else if (messageCreateEvent.getMessageContent().startsWith("%removefromping")){
            removePing(messageCreateEvent);
        }
    }

    /**
     * Pings group of given name.
     * @param event {@link MessageCreateEvent} Requesting message
     */
    private void ping(MessageCreateEvent event){
        String value = event.getMessageContent().split(" ", 2)[1];
        if (event.getServer().isEmpty()){
            return;
        }
        List<Role> roleList = event.getServer().get().getRolesByNameIgnoreCase(value);
        StringBuilder message = new StringBuilder();
        for (Role role : roleList){
            message.append(role.getMentionTag());
        }
        event.getChannel().sendMessage(message.toString());
    }

    /**
     * Adds {@link org.javacord.api.entity.user.User} to ping group
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void addPing(MessageCreateEvent event){
        String value = event.getMessageContent().split(" ", 2)[1];
        if (event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent()) {
            List<Role> roleList = event.getServer().get().getRolesByNameIgnoreCase(value);
            for (Role role : roleList){
                if (role.hasUser(event.getMessageAuthor().asUser().get())){
                    role.addUser(event.getMessageAuthor().asUser().get(), "User has been added to " + role.getName());
                }
            }
        }
    }

    /**
     * Removes {@link org.javacord.api.entity.user.User} from ping group
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void removePing(MessageCreateEvent event){
        String value = event.getMessageContent().split(" ", 2)[1];
        if (event.getServer().isPresent() && event.getMessageAuthor().asUser().isPresent()){
            List<Role> roleList = event.getServer().get().getRolesByNameIgnoreCase(value);
            for (Role role : roleList){
                if (role.hasUser(event.getMessageAuthor().asUser().get())){
                    role.removeUser(event.getMessageAuthor().asUser().get(), "User hes been removed from " + role.getName());
                }
            }
        }
    }

    /**
     * Creates ping group as {@link Role}
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void createPing(MessageCreateEvent event){
        if (event.getMessageAuthor().isRegularUser()){
            return;
        }
        String value = event.getMessageContent().split(" ", 2)[1];
        if (event.getServer().isPresent()){
            event.getServer().get().createRoleBuilder()
                    .setName(value)
                    .setMentionable(true)
                    .setAuditLogReason("Automatische Rollenerstellung")
                    .create()
                    .thenCompose(role -> event.getChannel().sendMessage("Role " + role.getName() + " has been created"));
        }
    }

    /**
     * Removes ping group.
     * @param event {@link MessageCreateEvent} requesting message.
     */
    private void removePings(MessageCreateEvent event){
        if (event.getMessageAuthor().isRegularUser()){
            return;
        }
        String value = event.getMessageContent().split(" ", 2)[1];
        if (event.getServer().isPresent()){
            event.getServer().get().getRolesByNameIgnoreCase(value)
                    .forEach(role -> role.delete()
                            .thenCompose(r -> event.getChannel().sendMessage("Role " + value + " has been removed"))
                    );
        }
    }
}
