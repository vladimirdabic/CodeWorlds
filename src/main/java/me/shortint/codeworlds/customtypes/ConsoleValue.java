package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.RunnableValue;
import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueProperties;
import org.bukkit.Bukkit;

public class ConsoleValue implements Value, ValueProperties {
    @Override
    public String getTypeName() {
        return "Console";
    }

    @Override
    public String toStringValue() {
        return "<Console>";
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
    public Value getProperty(String propertyName) {
        switch(propertyName) {
            case "sendMessage": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    for(Value value : args)
                        Bukkit.getServer().getConsoleSender().sendMessage(value.toStringValue());

                    return Interpreter.nullValue;
                }
            };

            case "execute": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    for(Value value : args)
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), value.toStringValue());

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
}
