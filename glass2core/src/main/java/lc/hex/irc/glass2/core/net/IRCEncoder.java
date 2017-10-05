package lc.hex.irc.glass2.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lc.hex.irc.glass2.api.IRCLine;

import java.util.List;

public class IRCEncoder extends MessageToMessageEncoder<IRCLine> {
    @Override
    protected void encode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        String s = msg.toString() + "\r\n";
        out.add(s);
    }
}
