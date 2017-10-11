package lc.hex.irc.glass2.core.net;

import com.google.inject.assistedinject.Assisted;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.IRCProxyFibre;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.IRCMessageEvent;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;

public class G2UpstreamProxyFibre extends ChannelInboundHandlerAdapter implements IRCProxyFibre {

    private Logger logger;
    private EventBus eventBus;
    private Channel upstream;
    private G2DownstreamProxyFibre downstream;
    private boolean ssl;
    private static SslContext sslContext;

    static {
        try {
            sslContext = SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }

    @Inject
    public G2UpstreamProxyFibre(Logger logger, EventBus eventBus, @Assisted G2DownstreamProxyFibre downstreamFibre, @Assisted boolean ssl) {
        this.logger = logger;
        this.eventBus = eventBus;
        this.downstream = downstreamFibre;
        this.ssl = ssl;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        upstream = ctx.channel();
    }

    @Override
    public void write(String message) {
        write(new IRCLine().read(message));
    }

    @Override
    public void writeAndFlush(String message) {
        writeAndFlush(new IRCLine().read(message));
    }

    @Override
    public void write(IRCLine line) {
        upstream.write(line);
    }

    @Override
    public void writeAndFlush(IRCLine line) {
        upstream.writeAndFlush(line);
    }

    @Override
    public Channel getDownstream() {
        return downstream.getDownstream();
    }

    @Override
    public Channel getUpstream() {
        return upstream;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object raw) throws Exception {
        IRCLine msg = (IRCLine) raw;
        eventBus.post(new IRCMessageEvent.Clientbound(msg, this));
        if (downstream != null) {
            logger.trace("<--S " + msg.toString());
            downstream.writeAndFlush(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (downstream != null && downstream.getDownstream().isOpen()) {
            downstream.writeAndFlush(IRCLine.proxyNotice("Lost connection to server!"));
            downstream.getDownstream().close();
        }
    }

    public static interface Factory {
        G2UpstreamProxyFibre create(G2DownstreamProxyFibre downstreamFibre, boolean ssl);
    }

    public static class Initializer extends ChannelInitializer<Channel> {

        private final G2DownstreamProxyFibre downstreamFibre;
        private final boolean ssl;
        private Logger logger;
        private Factory factory;
        private G2UpstreamProxyFibre fibre;

        public Initializer(G2DownstreamProxyFibre downstreamFibre, boolean ssl, Logger logger, G2UpstreamProxyFibre.Factory factory) {
            this.downstreamFibre = downstreamFibre;
            this.ssl = ssl;
            this.logger = logger;
            this.factory = factory;
        }

        @Override
        protected void initChannel(Channel ch) throws Exception {
            ch.pipeline()
                    .addLast("irc_enc", new IRCEncoder())
                    .addLast("str_enc", new StringEncoder(StandardCharsets.UTF_8))
                    .addLast("line_dec", new LineBasedFrameDecoder(1024))
                    .addLast("str_dec", new StringDecoder(StandardCharsets.UTF_8))
                    .addLast("irc_dec", new IRCDecoder())
                    .addLast("upstream", this.fibre = factory.create(downstreamFibre, ssl));
            if (ssl) {
                ch.pipeline().addBefore("line_dec", "ssl", sslContext.newHandler(ch.alloc()));
                logger.trace("Installed SSL handler!");
            }
            downstreamFibre.setUpstream(fibre);
        }
    }
}
