package lc.hex.irc.glass2.api.event;

import lc.hex.irc.glass2.api.IRCLine;
import lc.hex.irc.glass2.api.IRCProxyFibre;

public class IRCMessageEvent implements Event {
    private IRCLine line;
    private IRCProxyFibre fibre;

    public IRCMessageEvent(IRCLine line, IRCProxyFibre fibre) {
        this.line = line;
        this.fibre = fibre;
    }

    public IRCLine getLine() {
        return line;
    }

    public void setLine(IRCLine line) {
        this.line = line;
    }

    public IRCProxyFibre getFibre() {
        return fibre;
    }

    public static class Clientbound extends IRCMessageEvent {

        public Clientbound(IRCLine line, IRCProxyFibre fibre) {
            super(line, fibre);
        }
    }

    public static class Serverbound extends IRCMessageEvent {

        public Serverbound(IRCLine line, IRCProxyFibre fibre) {
            super(line, fibre);
        }
    }
}
