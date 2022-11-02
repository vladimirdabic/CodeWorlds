package me.shortint.cwlang.lexer;

public class LexerError extends RuntimeException {
    public LexerError(String message) {
        super(message);
    }
}
