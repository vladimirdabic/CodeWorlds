package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;
import me.shortint.cwlang.interpreter.types.ValueProperties;

public class BlockClassValue implements Value, ValueProperties, ValueClass {
    @Override
    public boolean valueIsInstance(Value value) {
        return value instanceof BlockValue;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        return null;
    }

    @Override
    public Value getProperty(String propertyName) {
        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return null;
    }

    @Override
    public String getTypeName() {
        return "Block";
    }

    @Override
    public String toStringValue() {
        return "<Block class>";
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
