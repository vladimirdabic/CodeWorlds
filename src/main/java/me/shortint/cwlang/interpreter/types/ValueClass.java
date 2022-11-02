package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;

public interface ValueClass {
    boolean valueIsInstance(Value value);
    Value castValueToSelf(Interpreter interpreter, int line, Value value);
}
