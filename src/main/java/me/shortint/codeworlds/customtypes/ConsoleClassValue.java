package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class ConsoleClassValue implements Value, ValueClass {
    @Override
    public String getTypeName() {
        return "Console";
    }

    @Override
    public String toStringValue() {
        return "<Console class>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public boolean valueIsInstance(Value value) {
        return value instanceof CommandSenderValue && ((CommandSenderValue) value).getBukkitSender() instanceof ConsoleCommandSender;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        if(value instanceof CommandSenderValue) {
            CommandSenderValue senderValue = (CommandSenderValue)value;

            if(!(senderValue.getBukkitSender() instanceof ConsoleCommandSender))
                return null;

            return new ConsoleValue();
        }

        return null;
    }
}
