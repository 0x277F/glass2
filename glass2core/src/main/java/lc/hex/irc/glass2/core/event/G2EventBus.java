package lc.hex.irc.glass2.core.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lc.hex.irc.glass2.api.event.Event;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.Metasubscribe;
import lc.hex.irc.glass2.api.event.Subscribe;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class G2EventBus implements EventBus {
    private Multimap<Class<? extends Event>, Consumer<? extends Event>> handlers = HashMultimap.create();
    private Logger logger;
    private MethodHandles.Lookup lookup;

    @Inject
    public G2EventBus(Logger logger) {
        this.logger = logger;
        this.lookup = MethodHandles.lookup();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void post(T event) {
        handlers.get(event.getClass()).stream().map(c -> (Consumer<T>) c).forEach(c -> c.accept(event));
    }

    @SafeVarargs
    @Override
    public final <T extends Event> void subscribe(Consumer<T> handler, Class<? extends Event>... types) {
        Arrays.stream(types).forEach(cl -> handlers.put(cl, handler));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribe(Object object) {
        Class<?> clazz = object.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(Subscribe.class) && m.getParameterCount() == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
                Class<? extends Event>[] types;
                if (m.isAnnotationPresent(Metasubscribe.class)) {
                    types = (Class<? extends Event>[]) Arrays.stream(m.getAnnotationsByType(Metasubscribe.class)).map(Metasubscribe::value).collect(Collectors.toList()).toArray();
                } else {
                    types = new Class[]{m.getParameterTypes()[0]};
                }
                try {
                    MethodHandle handle = lookup.unreflect(m);
                    MethodType acceptType = MethodType.methodType(Void.class, Object.class);
                    CallSite callSite = LambdaMetafactory.metafactory(lookup, "accept", MethodType.methodType(Consumer.class), acceptType, handle, acceptType);
                    MethodHandle subscriber = callSite.getTarget();
                    this.subscribe((Consumer<? extends Event>) subscriber.invoke(), types);
                } catch (Throwable e) {
                    logger.throwing(e);
                }
            }
        }
    }
}
