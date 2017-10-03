package lc.hex.irc.glass2.core.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.codec.string.StringDecoder;
import lc.hex.irc.glass2.api.ProxyServer;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.core.G2CoreEventHandler;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class G2ProxyServer extends ChannelInitializer<SocketChannel> implements ProxyServer, Runnable {
    private NioEventLoopGroup serverLoop, childLoop;
    private Logger logger;
    private EventBus eventBus;
    private G2UpstreamProxyFibre.Factory factory;

    @Inject
    public G2ProxyServer(Logger logger, EventBus eventBus, G2UpstreamProxyFibre.Factory factory, G2CoreEventHandler eventHandler) {
        this.logger = logger;
        this.eventBus = eventBus;
        this.factory = factory;
        this.serverLoop = new NioEventLoopGroup(3);
        this.childLoop = new NioEventLoopGroup(4);
        eventBus.subscribe(eventHandler);
    }

    @Override
    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            logger.info("Starting proxy server...");
            bootstrap.channel(NioServerSocketChannel.class)
                    .group(serverLoop, childLoop)
                    .childHandler(this)
                    .bind(8841)
                    .sync()
                    .channel()
                    .closeFuture()
                    .sync();
        } catch (InterruptedException e) {
            logger.catching(e);
        } finally {
            serverLoop.shutdownGracefully();
            childLoop.shutdownGracefully();
        }
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        logger.trace("Initializing socket channel " + ch.remoteAddress().getHostName());
        ch.pipeline().addLast("frame_dec", new LineBasedFrameDecoder(1024))
                .addLast("str_dec", new StringDecoder())
                .addLast("irc_cod", new IRCCodec(logger))
                .addLast("irc_hlr", new G2DownstreamProxyFibre(logger, this, eventBus, factory))
                .addLast("line_enc", new LineEncoder(LineSeparator.WINDOWS));
        logger.trace("Established pipeline for channel " + ch.remoteAddress().getHostName());
    }

    public NioEventLoopGroup getChildLoop() {
        return childLoop;
    }
}
