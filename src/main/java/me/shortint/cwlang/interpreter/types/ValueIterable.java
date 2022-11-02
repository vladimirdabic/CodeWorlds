package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.parser.Statement;

public interface ValueIterable {
    void iterate(Interpreter interpreter, Statement body, String variableName);
}
