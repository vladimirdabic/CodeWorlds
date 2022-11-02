package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;

public interface ValueCallable {
    Value run(Interpreter interpreter, Value[] args, int line);
}
