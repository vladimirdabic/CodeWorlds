package me.shortint.cwlang.interpreter.types;


public abstract class RunnableValue implements Value, ValueCallable {

    @Override
    public String getTypeName() {
        return "function";
    }

    @Override
    public String toStringValue() {
        return "<function_wrapper>";
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
