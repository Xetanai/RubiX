package moe.xetanai.rubix.entities;

import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Module {
    public void onMessageReceived(ManagedEvent<MessageReceivedEvent> event) {};
    public void onGuildMemberJoin(ManagedEvent<GuildMemberJoinEvent> event) {};

    @SuppressWarnings ("unchecked")
    public void onEvent(ManagedEvent event) {
        if(event.getEvent() instanceof MessageReceivedEvent)
            onMessageReceived(event);
        if(event.getEvent() instanceof GuildMemberJoinEvent)
            onGuildMemberJoin(event);
    }
}
