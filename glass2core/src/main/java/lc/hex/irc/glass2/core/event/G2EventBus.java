package lc.hex.irc.glass2.core.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lc.hex.irc.glass2.api.event.Event;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.Metasubscribe;
import lc.hex.irc.glass2.api.event.Subscribe;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
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
        handlers.get(event.getClass()).stream().map(c -> (Consumer<T>) c).forEach(c -> {
            c.accept(event);
            logger.trace("event dispatch {} -> {}", event.getClass().getName(), c.getClass().getName());
        });
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
                Class<? extends Event>[] types = new Class[]{m.getParameterTypes()[0]};
                if (m.isAnnotationPresent(Metasubscribe.class)) {
                    List<Class<?>> classList = Arrays.stream(m.getAnnotationsByType(Metasubscribe.class)).map(Metasubscribe::value).collect(Collectors.toList());
                    classList.add(0, types[0]);
                    types = (Class<? extends Event>[]) classList.toArray();
                }
                try {
                    logger.trace("Attempting to process lambda conversion for " + m.getDeclaringClass().getName() + "#" + m.getName() + ": " + Arrays.toString(types));
                    MethodHandle handle = lookup.unreflect(m);
                    MethodType acceptType = handle.type();
                    acceptType = acceptType.dropParameterTypes(0, 1);
                    CallSite callSite = LambdaMetafactory.metafactory(lookup,
                            "accept",
                            MethodType.methodType(Consumer.class, clazz),
                            acceptType.changeParameterType(0, Object.class),
                            handle,
                            acceptType);
                    MethodHandle subscriber = callSite.getTarget();
                    Consumer<? extends Event> handler = (Consumer<? extends Event>) subscriber.invoke(object);
                    this.subscribe(handler, types);
                } catch (Throwable e) {
                    logger.throwing(e);
                }
            }
        }
    }
}
