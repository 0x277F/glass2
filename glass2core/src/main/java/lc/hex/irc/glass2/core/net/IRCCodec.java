package lc.hex.irc.glass2.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lc.hex.irc.glass2.api.IRCLine;

import java.util.List;

public class IRCCodec extends MessageToMessageCodec<String, IRCLine> {
    @Override
    protected void encode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        out.add(msg.toString());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        IRCLine line = new IRCLine();
        line.read(msg);
        out.add(line);
    }
}
