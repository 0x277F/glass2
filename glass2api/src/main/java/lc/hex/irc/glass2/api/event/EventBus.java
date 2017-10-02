package lc.hex.irc.glass2.api.event;

import java.util.function.Consumer;

public interface EventBus {
    <T extends Event> void post(T event);

    <T extends Event> void subscribe(Consumer<T> handler, Class<? extends Event>... types);

    void subscribe(Object object);
}