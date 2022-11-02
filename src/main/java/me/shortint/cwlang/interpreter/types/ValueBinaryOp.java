package me.shortint.cwlang.interpreter.types;

public interface ValueBinaryOp {
    Value add(Value other);
    Value sub(Value other);
    Value mul(Value other);
    Value div(Value other);
    Value lt(Value other);
    Value lte(Value other);
}
