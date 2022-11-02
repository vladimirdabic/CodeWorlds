package me.shortint.cwlang.interpreter.types;

public class NullValue implements Value {
    @Override
    public String getTypeName() {
        return "null";
    }

    @Override
    public String toStringValue() {
        return "null";
    }

    @Override
    public boolean equals(Value other) {
        return other instanceof NullValue;
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullValue;
    }
}
