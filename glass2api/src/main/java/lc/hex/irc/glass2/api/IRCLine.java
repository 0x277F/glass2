package lc.hex.irc.glass2.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class IRCLine {
    private String prefix;
    private String command;
    private List<String> params = new ArrayList<>();
    private String trailing;
    private boolean numeric;

    public static IRCLine proxyNotice(String message) {
        return new IRCLine().setPrefix("glass.hex.lc").setCommand("NOTICE").setParams(Collections.singletonList("Auth")).setTrailing(message);
    }

    public String getPrefix() {
        return prefix;
    }

    public IRCLine setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public IRCLine setCommand(String command) {
        this.command = command;
        try {
            int numeric = Integer.parseInt(command);
            this.numeric = true;
        } catch (Throwable ignored) {

        }
        return this;
    }

    public List<String> getParams() {
        return params;
    }

    public IRCLine setParams(List<String> params) {
        this.params = params;
        return this;
    }

    public String getTrailing() {
        return trailing;
    }

    public IRCLine setTrailing(String trailing) {
        this.trailing = trailing;
        return this;
    }

    public String getLastElement() {
        return trailing == null ? params.get(params.size() - 1) : trailing;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public int getNumeric() {
        return Integer.parseInt(command);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(":" + prefix + " ");
        }
        sb.append(command + " ");
        params.forEach(s -> sb.append(s + " "));
        if (trailing != null) {
            sb.append(trailing);
        }
        return sb.toString();
    }

    public IRCLine read(String input) {
        String[] split = input.split(" ");
        int i = 0;
        if (split[0].startsWith(":")) {
            prefix = split[0].substring(1);
            i++;
        }
        command = split[i];
        for (int j = ++i; j < split.length; j++) {
            String s = split[j];
            if (s.startsWith(":")) {
                trailing = Arrays.stream(Arrays.copyOfRange(split, j, split.length)).collect(Collectors.joining(" "));
                break;
            } else {
                params.add(s);
            }
        }
        return this;
    }
}
