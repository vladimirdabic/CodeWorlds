package me.shortint.cwlang.interpreter;

import me.shortint.cwlang.interpreter.types.Value;

public class ReturnError extends RuntimeException {
    public final Value value;

    public ReturnError(Value value) {
        this.value = value;
    }
}
