package lc.hex.irc.glass2.core;

import io.netty.channel.ChannelFutureListener;
import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.IRCMessageEvent;
import lc.hex.irc.glass2.api.event.Subscribe;
import lc.hex.irc.glass2.core.net.G2DownstreamProxyFibre;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class G2CoreEventHandler {
    private static final String AUTH_DELIM = "-";
    private Logger logger;
    private EventBus eventBus;
    private String pass;

    @Inject
    public G2CoreEventHandler(Logger logger, EventBus eventBus) {
        this.logger = logger;
        this.eventBus = eventBus;
    }

    @Subscribe
    public void onInboundDownstreamMessage(IRCMessageEvent.Serverbound event) {
        IRCLine line = event.getLine();
        logger.trace("start processing downstream: " + event.getLine().toString());
        G2DownstreamProxyFibre fibre = ((G2DownstreamProxyFibre) event.getFibre());
        if (!fibre.isConnected() && line.getCommand().equalsIgnoreCase("PASS")) {
            String[] split = event.getLine().getParams().get(0).split(AUTH_DELIM);
            if (split.length < 2) {
                fibre.writeAndFlush(IRCLine.proxyNotice("Insufficient parameters to PASS:"));
                fibre.writeAndFlush(IRCLine.proxyNotice("PASS: usage: /PASS <server> [+]<port> <pass> with spaces representing unit separators."));
            } else {
                String host = split[0];
                boolean ssl = false;
                int port;
                if (split[1].startsWith("+")) {
                    ssl = true;
                    port = Integer.parseInt(split[1].substring(1));
                } else {
                    port = Integer.parseInt(split[1]);
                }
                if (split.length == 3) {
                    pass = split[2];
                }
                fibre.synchronizeUpstream(host, port, ssl).addListener((ChannelFutureListener) f -> {
                    if (f.isSuccess() && pass != null) {
                        f.channel().writeAndFlush(new IRCLine().read("PASS " + pass));
                    }
                });
            }
        }
    }
}
