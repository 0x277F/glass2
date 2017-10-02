package lc.hex.irc.glass2.api.event;

import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.IRCProxyFibre;

public class IRCMessageEvent implements Event {
    private IRCLine line;
    private Side side;
    private IRCProxyFibre fibre;

    public IRCMessageEvent(Side side, IRCLine line, IRCProxyFibre fibre) {
        this.line = line;
        this.side = side;
        this.fibre = fibre;
    }

    public IRCLine getLine() {
        return line;
    }

    public void setLine(IRCLine line) {
        this.line = line;
    }

    public Side getSide() {
        return side;
    }

    public IRCProxyFibre getFibre() {
        return fibre;
    }

    public enum Side {
        UPSTREAM, DOWNSTREAM
    }

    public static class Inbound extends IRCMessageEvent {

        public Inbound(Side side, IRCLine line, IRCProxyFibre fibre) {
            super(side, line, fibre);
        }
    }

    public static class Outbound extends IRCMessageEvent {

        public Outbound(Side side, IRCLine line, IRCProxyFibre fibre) {
            super(side, line, fibre);
        }
    }
}
