package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;

public class WorldClassValue implements Value, ValueClass {
    @Override
    public boolean valueIsInstance(Value value) {
        return value instanceof WorldValue;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        return null;
    }

    @Override
    public String getTypeName() {
        return "World";
    }

    @Override
    public String toStringValue() {
        return "<World>";
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
