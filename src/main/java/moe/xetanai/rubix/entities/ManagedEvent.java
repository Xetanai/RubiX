package moe.xetanai.rubix.entities;

import net.dv8tion.jda.core.events.Event;

public class ManagedEvent<T extends Event> {
    private boolean handled = false;
    private T baseEvent;

    public ManagedEvent(T baseEvent) {
        this.baseEvent = baseEvent;
    }

    public T getEvent() {
        return this.baseEvent;
    }

    public void handle() {
        this.handled = true;
    }

    public boolean isHandled() {
        return this.handled;
    }
}
