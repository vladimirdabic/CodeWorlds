package me.shortint.codeworlds.customtypes;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;
import org.bukkit.entity.Player;

public class PlayerClassValue implements Value, ValueClass {
    @Override
    public boolean valueIsInstance(Value value) {
        if(value instanceof CommandSenderValue && ((CommandSenderValue)value).getBukkitSender() instanceof Player)
            return true;

        return value instanceof PlayerObjectValue;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        if(value instanceof CommandSenderValue) {
            CommandSenderValue senderValue = (CommandSenderValue)value;

            if(!(senderValue.getBukkitSender() instanceof Player))
                return null;

            return new PlayerObjectValue((Player)senderValue.getBukkitSender());
        }

        return null;
    }

    @Override
    public String getTypeName() {
        return "Player";
    }

    @Override
    public String toStringValue() {
        return "<Player>";
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
