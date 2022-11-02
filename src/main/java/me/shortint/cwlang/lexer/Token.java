package me.shortint.cwlang.lexer;

public class Token {
    public final TokenType type;
    public final Object literal;
    public final String lexeme;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.line = line;
        this.literal = literal;
        this.type = type;
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", literal=" + literal +
                ", lexeme='" + lexeme + '\'' +
                ", line=" + line +
                '}';
    }
}
