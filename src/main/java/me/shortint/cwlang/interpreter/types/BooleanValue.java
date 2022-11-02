package me.shortint.cwlang.interpreter.types;

public class BooleanValue implements Value {
    private final boolean value;

    public BooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public String getTypeName() {
        return "boolean";
    }

    @Override
    public String toStringValue() {
        return value ? "true" : "false";
    }

    @Override
    public boolean equals(Value other) {
        if(!(other instanceof BooleanValue)) return false;
        return value == ((BooleanValue)other).value;
    }

    @Override
    public Value copy() {
        // boolean values are never edited directly so no need to copy them
        return this;
    }

    public boolean getValue() {
        return value;
    }
}
