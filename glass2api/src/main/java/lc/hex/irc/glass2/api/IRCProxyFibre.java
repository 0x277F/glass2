package lc.hex.irc.glass2.api;

import io.netty.channel.Channel;

public interface IRCProxyFibre {
    void write(String message);

    void writeAndFlush(String message);

    void write(IRCLine line);

    void writeAndFlush(IRCLine line);

    Channel getDownstream();

    Channel getUpstream();

    boolean isConnected();
}
