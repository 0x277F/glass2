package lc.hex.irc.glass2.api.event;

import lc.hex.irc.glass2.api.IRCProxyFibre;

public class DownstreamInitEvent implements Event {

    private final IRCProxyFibre fibre;

    public DownstreamInitEvent(IRCProxyFibre fibre) {
        this.fibre = fibre;
    }

    public IRCProxyFibre getFibre() {
        return fibre;
    }
}
