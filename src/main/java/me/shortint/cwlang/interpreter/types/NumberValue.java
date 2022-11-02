package me.shortint.cwlang.interpreter.types;

import java.util.Objects;

public class NumberValue implements Value, ValueBinaryOp {
    private double value;

    public NumberValue(double value) {
        this.value = value;
    }

    @Override
    public Value add(Value other) {
        if(other instanceof NumberValue) {
            return new NumberValue(value + ((NumberValue)other).value);
        }

        if(other instanceof StringValue) {
            return new StringValue(toStringValue() + ((StringValue)other).getValue());
        }

        return null;
    }

    @Override
    public Value sub(Value other) {
        if(other instanceof NumberValue) {
            return new NumberValue(value - ((NumberValue)other).value);
        }

        return null;
    }

    @Override
    public Value mul(Value other) {
        if(other instanceof NumberValue) {
            return new NumberValue(value * ((NumberValue)other).value);
        }

        if(other instanceof StringValue) {
            return new StringValue(((StringValue)other).getValue().repeat((int)value));
        }

        return null;
    }

    @Override
    public Value div(Value other) {
        if(other instanceof NumberValue) {
            return new NumberValue(value / ((NumberValue)other).value);
        }

        return null;
    }

    @Override
    public Value lt(Value other) {
        if(other instanceof NumberValue) {
            return new BooleanValue(value < ((NumberValue)other).value);
        }

        return null;
    }

    @Override
    public Value lte(Value other) {
        if(other instanceof NumberValue) {
            return new BooleanValue(value <= ((NumberValue)other).value);
        }

        return null;
    }

    @Override
    public String getTypeName() {
        return "number";
    }

    @Override
    public String toStringValue() {
        return value % 1 == 0 ? Integer.toString((int)value) : Double.toString(value);
    }

    @Override
    public boolean equals(Value other) {
        if(!(other instanceof NumberValue)) return false;
        return value == ((NumberValue)other).value;
    }

    @Override
    public Value copy() {
        // number values are never edited directly so no need to copy them
        // binary operations already make a new value
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberValue that = (NumberValue) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public double getValue() {
        return value;
    }
}
