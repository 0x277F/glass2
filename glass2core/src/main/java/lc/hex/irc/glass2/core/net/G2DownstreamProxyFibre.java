package lc.hex.irc.glass2.core.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.IRCProxyFibre;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.IRCMessageEvent;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.util.List;

public class G2DownstreamProxyFibre extends MessageToMessageCodec<IRCLine, IRCLine> implements IRCProxyFibre {

    private Logger logger;
    private G2ProxyServer proxyServer;
    private EventBus eventBus;
    private G2UpstreamProxyFibre.Factory factory;
    private Channel downstream, upstream;
    private SslContext sslContext;

    @Inject
    public G2DownstreamProxyFibre(Logger logger, G2ProxyServer proxyServer, EventBus eventBus, G2UpstreamProxyFibre.Factory factory) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.eventBus = eventBus;
        this.factory = factory;
        try {
            this.sslContext = SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            this.logger.throwing(e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.downstream = ctx.channel();
        logger.trace("channelActive");
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
        downstream.write(line);
    }

    @Override
    public void writeAndFlush(IRCLine line) {
        downstream.writeAndFlush(line);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        eventBus.post(new IRCMessageEvent.Outbound(IRCMessageEvent.Side.DOWNSTREAM, msg, this));
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        eventBus.post(new IRCMessageEvent.Inbound(IRCMessageEvent.Side.DOWNSTREAM, msg, this));
        if (upstream != null) {
            logger.trace("--> " + msg.toString());
            upstream.writeAndFlush(msg);
        }
    }

    @Override
    public Channel getDownstream() {
        return downstream;
    }

    @Override
    public Channel getUpstream() {
        return upstream;
    }

    public ChannelFuture synchronizeUpstream(String host, int port, boolean ssl) {
        Bootstrap bootstrap = new Bootstrap();
        return bootstrap.group(proxyServer.getChildLoop())
                .channel(NioSocketChannel.class)
                .handler(factory.create(this))
                .connect(host, port)
                .addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                logger.info("Successfully synchronized to " + host + ":" + port);
                Channel channel = f.channel();
                G2UpstreamProxyFibre upstreamFibre = channel.pipeline().get(G2UpstreamProxyFibre.class);
                this.upstream = upstreamFibre.getUpstream();
                if (ssl) {
                    channel.pipeline().addBefore("line-dec", "ssl", sslContext.newHandler(channel.alloc()));
                }
            } else {
                writeAndFlush(IRCLine.proxyNotice("Error connecting to server, please check your logs."));
                logger.throwing(f.cause());
            }
        });
    }
}
