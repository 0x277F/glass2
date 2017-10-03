package lc.hex.irc.glass2.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lc.hex.irc.glass2.api.IRCLine;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.List;

public class IRCCodec extends MessageToMessageCodec<String, IRCLine> {

    private Logger logger;

    @Inject
    public IRCCodec(Logger logger) {

        this.logger = logger;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IRCLine msg, List<Object> out) throws Exception {
        out.add(msg.toString());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        IRCLine line = new IRCLine();
        line.read(msg);
        out.add(line);
        logger.trace("decoded " + msg + " to " + line.toString());
    }
}
