package lc.hex.irc.glass2.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lc.hex.irc.glass2.api.IRCLine;

import java.util.List;

public class IRCDecoder extends MessageToMessageDecoder<String> {
    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        out.add(new IRCLine().read(msg));
    }
}
