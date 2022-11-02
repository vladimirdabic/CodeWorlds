package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;
import org.bukkit.ChatColor;

import java.util.Objects;

public class StringValue implements Value, ValueBinaryOp, ValueIndexable, ValueProperties {
    private String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public Value add(Value other) {
        if(other instanceof StringValue) {
            return new StringValue(value + ((StringValue)other).value);
        }

        if(other instanceof NumberValue) {
            return new StringValue(value + other.toStringValue());
        }

        return null;
    }

    @Override
    public Value sub(Value other) {
        return null;
    }

    @Override
    public Value mul(Value other) {
        if(other instanceof NumberValue) {
            return new StringValue(value.repeat((int)((NumberValue)other).getValue()));
        }

        return null;
    }

    @Override
    public Value div(Value other) {
        return null;
    }

    @Override
    public Value lt(Value other) {
        return null;
    }

    @Override
    public Value lte(Value other) {
        return null;
    }

    @Override
    public String getTypeName() {
        return "string";
    }

    @Override
    public String toStringValue() {
        return value;
    }

    @Override
    public boolean equals(Value other) {
        if(!(other instanceof StringValue)) return false;
        return value.equals(((StringValue)other).value);
    }

    @Override
    public Value getAtIndex(Value index) {
        if(!(index instanceof NumberValue))
            return null;

        NumberValue indexNumber = (NumberValue)index;

        if(indexNumber.getValue() % 1 != 0)
            return null;

        int idx = (int)indexNumber.getValue();

        if(idx < 0 || idx >= value.length()) return Interpreter.nullValue;
        return new StringValue(Character.toString(value.charAt(idx)));
    }

    @Override
    public Value setAtIndex(Value index, Value value) {
        if(!(index instanceof NumberValue))
            return null;

        NumberValue indexNumber = (NumberValue)index;

        if(indexNumber.getValue() % 1 != 0)
            return null;

        int idx = (int)indexNumber.getValue();

        if(idx < 0 || idx >= this.value.length()) return null;
        this.value = this.value.substring(0, idx) + value.toStringValue() + this.value.substring(idx + 1);
        return new StringValue(this.value);
    }

    @Override
    public Value getProperty(String propertyName) {

        switch (propertyName) {
            case "colored": return new StringValue(ChatColor.translateAlternateColorCodes('&', value));
            case "substring": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("string->substring", false, line, args, "number", "number");

                    if(args.length == 1) {
                        NumberValue start = (NumberValue)args[0];
                        return new StringValue(value.substring((int)start.getValue()));
                    } else if(args.length == 2) {
                        NumberValue start = (NumberValue)args[0];
                        NumberValue end = (NumberValue)args[1];
                        return new StringValue(value.substring((int)start.getValue(), (int)end.getValue()));
                    }

                    return null;
                }
            };

            case "replace": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("string->replace", true, line, args, "string", "string");
                    StringValue target = (StringValue)args[0];
                    StringValue replacement = (StringValue)args[1];
                    return new StringValue(value.replace(target.value, replacement.value));
                }
            };

            case "contains": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("string->replace", true, line, args, "string");
                    StringValue target = (StringValue)args[0];
                    return new BooleanValue(value.contains(target.value));
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
        return new StringValue(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringValue that = (StringValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String getValue() {
        return value;
    }
}
