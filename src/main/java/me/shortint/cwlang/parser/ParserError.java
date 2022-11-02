package me.shortint.cwlang.parser;

public class ParserError extends RuntimeException {
    public ParserError(String message) {
        super(message);
    }
}
