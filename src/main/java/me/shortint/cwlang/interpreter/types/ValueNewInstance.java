package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;

public interface ValueNewInstance {
    Value newInstance(Interpreter interpreter, Value[] args, int line);
}
