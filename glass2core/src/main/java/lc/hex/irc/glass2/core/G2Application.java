package lc.hex.irc.glass2.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lc.hex.irc.glass2.api.ProxyServer;

public class G2Application {
    public static void main(String[] args) {
        final Injector injector = Guice.createInjector(new G2CoreModule());
        injector.getInstance(ProxyServer.class);
    }
}
