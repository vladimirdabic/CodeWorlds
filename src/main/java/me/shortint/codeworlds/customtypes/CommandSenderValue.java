package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.RunnableValue;
import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueProperties;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSenderValue implements Value, ValueProperties {
    private final CommandSender sender;

    public CommandSenderValue(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getTypeName() {
        return "CommandSender";
    }

    @Override
    public String toStringValue() {
        return "<CommandSender_instance>";
    }

    public CommandSender getBukkitSender() {
        return sender;
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value getProperty(String propertyName) {
        switch (propertyName) {
            case "name": return new StringValue(sender.getName());
            case "sendMessage": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("CommandSender->sendMessage", true, line, args, "string");
                    StringValue message = (StringValue)args[0];
                    sender.sendMessage(message.getValue());
                    return Interpreter.nullValue;
                }
            };
        }

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return null;
    }

    @Override
    public Value copy() {
        return this;
    }
}
