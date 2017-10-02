package lc.hex.irc.glass2.api;

public interface ProxyModule {
    String getName();

    String getVersion();

    void enable();

    void disable();
}
