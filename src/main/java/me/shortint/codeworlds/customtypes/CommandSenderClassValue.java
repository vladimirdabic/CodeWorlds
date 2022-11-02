package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;
import org.bukkit.Bukkit;

public class CommandSenderClassValue implements Value, ValueClass {
    @Override
    public boolean valueIsInstance(Value value) {
        return (value instanceof CommandSenderValue || value instanceof PlayerObjectValue || value instanceof ConsoleValue);
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        if(value instanceof CommandSenderValue)
            return value;

        if(value instanceof PlayerObjectValue)
            return new CommandSenderValue(((PlayerObjectValue)value).getBukkitPlayer());

        if(value instanceof ConsoleValue)
            return new CommandSenderValue(Bukkit.getServer().getConsoleSender());

        return null;
    }

    @Override
    public String getTypeName() {
        return "CommandSender";
    }

    @Override
    public String toStringValue() {
        return "<CommandSender>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value copy() {
        return this;
    }
}
