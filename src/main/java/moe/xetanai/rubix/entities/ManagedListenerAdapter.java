package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.modules.AntiSpamModule;
import moe.xetanai.rubix.modules.CommandModule;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ManagedListenerAdapter extends ListenerAdapter {
    private List<Module> modules = new ArrayList<>();

    public ManagedListenerAdapter() {
        registerModules();
    }

    @Override
    public void onGenericEvent(Event event) {
        // Convert JDA events into our managed event format, then pass them to their respective modules
        ManagedEvent managed = null;

        if(event instanceof MessageReceivedEvent) {
            managed = new ManagedEvent<>((MessageReceivedEvent) event);
        }

        if(managed != null)
            handleEvent(managed);
    }

    public void handleEvent(ManagedEvent event) {
        for(Module m : modules) {
            if(event.isHandled())
                break;
            m.onEvent(event);
        }
    }

    public void registerModules() {
        modules.add(new AntiSpamModule());
        modules.add(new CommandModule());
    }
}
