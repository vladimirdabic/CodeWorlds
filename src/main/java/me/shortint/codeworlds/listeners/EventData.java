package me.shortint.codeworlds.listeners;

import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.parser.Statement;

public class EventData {
    public final String eventName;
    public final String[] eventArguments;
    public final Statement eventBody;
    public final Environment eventEnvironment;
    public final String eventContext;

    public EventData(final String eventName, final String[] eventArguments, final Statement eventBody, final Environment eventEnvironment, final String eventContext) {
        this.eventName = eventName;
        this.eventArguments = eventArguments;
        this.eventBody = eventBody;
        this.eventEnvironment = eventEnvironment;
        this.eventContext = eventContext;
    }
}
