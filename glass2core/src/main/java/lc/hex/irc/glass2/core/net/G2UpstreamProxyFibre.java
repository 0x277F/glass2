package lc.hex.irc.glass2.core.net;

import com.google.inject.assistedinject.Assisted;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.IRCProxyFibre;
import lc.hex.irc.glass2.api.event.EventBus;
import lc.hex.irc.glass2.api.event.IRCMessageEvent;

import javax.inject.Inject;
import java.util.List;

public class G2UpstreamProxyFibre extends MessageToMessageCodec<IRCLine, IRCLine> implements IRCProxyFibre {

    private EventBus eventBus;
    private G2DownstreamProxyFibre downstreamFibre;
    private Channel upstream, downstream;

    @Inject
    public G2UpstreamProxyFibre(EventBus eventBus, @Assisted G2DownstreamProxyFibre downstreamFibre) {
        this.eventBus = eventBus;
        this.downstreamFibre = downstreamFibre;
        this.downstream = downstreamFibre.getDownstream();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().pipeline().addLast("irc-cod", new IRCCodec()).addLast("line-enc", new LineEncoder(LineSeparator.WINDOWS));
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
        return upstream;
    }

    @Override
    public Channel getUpstream() {
        return downstream;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        eventBus.post(new IRCMessageEvent.Outbound(IRCMessageEvent.Side.UPSTREAM, msg, this));
        out.add(msg);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        eventBus.post(new IRCMessageEvent.Inbound(IRCMessageEvent.Side.UPSTREAM, msg, this));
        out.add(msg);
    }

    public static interface Factory {
        G2UpstreamProxyFibre create(G2DownstreamProxyFibre downstreamFibre);
    }
}
