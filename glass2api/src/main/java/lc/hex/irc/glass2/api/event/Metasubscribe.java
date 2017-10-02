package lc.hex.irc.glass2.api.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Metasubscriptions.class)
public @interface Metasubscribe {
    Class<? extends Event> value();
}
