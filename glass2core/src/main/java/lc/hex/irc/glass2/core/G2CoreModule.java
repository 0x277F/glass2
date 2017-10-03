package lc.hex.irc.glass2.core;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import lc.hex.irc.glass2.api.IRCProxyFibre;
import lc.hex.irc.glass2.api.ProxyModule;
import lc.hex.irc.glass2.api.ProxyServer;
import lc.hex.irc.glass2.api.ServerProperties;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.core.event.G2EventBus;
import lc.hex.irc.glass2.core.net.G2DownstreamProxyFibre;
import lc.hex.irc.glass2.core.net.G2ProxyServer;
import lc.hex.irc.glass2.core.net.G2UpstreamProxyFibre;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class G2CoreModule extends AbstractModule implements ProxyModule {
    @Override
    protected void configure() {
        bind(G2CoreModule.class).toInstance(this);
        bind(Logger.class).toInstance(LogManager.getContext().getLogger("glass"));
        install(new FactoryModuleBuilder().build(G2UpstreamProxyFibre.Factory.class));
        bind(ProxyServer.class).to(G2ProxyServer.class);
        bind(IRCProxyFibre.class).to(G2DownstreamProxyFibre.class);
        bind(EventBus.class).to(G2EventBus.class);
    }

    @Override
    public String getName() {
        return "g2core";
    }

    @Override
    public String getVersion() {
        return ServerProperties.VERSION;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {

    }
}
