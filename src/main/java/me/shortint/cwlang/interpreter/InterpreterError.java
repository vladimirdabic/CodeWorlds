package me.shortint.cwlang.interpreter;

public class InterpreterError extends RuntimeException {
    public InterpreterError(String message) {
        super(message);
    }
}
