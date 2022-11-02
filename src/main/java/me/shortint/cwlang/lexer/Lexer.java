package me.shortint.cwlang.lexer;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private static final HashMap<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("func", TokenType.FUNC);
        keywords.put("if", TokenType.IF);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("var", TokenType.VAR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("else", TokenType.ELSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("this", TokenType.THIS);
        keywords.put("super", TokenType.SUPER);
        keywords.put("new", TokenType.NEW);
        keywords.put("class", TokenType.CLASS);
        keywords.put("is", TokenType.IS);
        keywords.put("event", TokenType.EVENT);
        keywords.put("command", TokenType.COMMAND);
        keywords.put("static", TokenType.STATIC);
    }

    private int current;
    private int start;
    private int line;
    private final ArrayList<Token> tokens = new ArrayList<>();
    private String source;
    private String context;

    public Token[] lex(String source, String context) {
        this.source = source;
        this.context = context;
        this.current = 0;
        this.line = 1;
        this.tokens.clear();

        while(available()) {
            start = current;
            scanToken();
        }

        addToken(TokenType.EOF);
        return tokens.toArray(new Token[0]);
    }


    private void scanToken() {
        char c = advance();

        switch (c) {
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '/':
                if(match('/')) {
                    while(peek() != '\n' && available()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case '(': addToken(TokenType.OPEN_PAREN); break;
            case ')': addToken(TokenType.CLOSE_PAREN); break;
            case '{': addToken(TokenType.OPEN_BRACE); break;
            case '}': addToken(TokenType.CLOSE_BRACE); break;
            case '[': addToken(TokenType.OPEN_SQUARE); break;
            case ']': addToken(TokenType.CLOSE_SQUARE); break;

            case ';': addToken(TokenType.SEMICOLON); break;
            case ':': addToken(TokenType.COLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.':
                if(peek() == '.' && peek(1) == '.') {
                    advance();
                    advance();
                    addToken(TokenType.VARARGS);
                } else {
                    addToken(TokenType.DOT);
                }
                break;

            case '=': addToken(match('=') ? TokenType.DOUBLE_EQUALS : TokenType.EQUALS); break;
            case '!': addToken(match('=') ? TokenType.NOT_EQUALS : TokenType.BANG); break;
            case '>': addToken(match('=') ? TokenType.GT_EQUALS : TokenType.GREATER); break;
            case '<': addToken(match('=') ? TokenType.LT_EQUALS : TokenType.LOWER); break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                ++line;
                break;

            case '"': scanString('"'); break;
            case '\'': scanString('\''); break;

            default:
                if(Character.isDigit(c))
                    scanNumber();
                else if(Character.isLetter(c) || c == '_')
                    scanIdentifier();
                else
                    throw error("Unexpected character '" + c + "'");
        }
    }

    private void scanNumber() {
        while(Character.isDigit(peek())) advance();

        if(match('.')) {
            while(Character.isDigit(peek())) advance();
        }

        String number = source.substring(start, current);
        double value = Double.parseDouble(number);

        addToken(TokenType.NUMBER_LITERAL, value);
    }

    private void scanString(char closing) {
        StringBuilder builder = new StringBuilder();

        while(peek() != closing) {
            char c = advance();

            if(c == '\\') {
                if(peek() == closing)
                    c = closing;

                switch (peek()) {
                    case '\\': c = '\\'; break;
                    case 'n': c = '\n'; break;
                    case 't': c = '\t'; break;
                    case 'r': c = '\r'; break;
                    case '0': c = '\0'; break;
                    case 'x': {
                        advance();
                        char d1 = advance();
                        char d2 = peek();
                        c = (char)(int)Integer.valueOf(d1 + String.valueOf(d2), 16);
                    } break;
                    case 'u':
                        advance();
                        char d1 = advance();
                        char d2 = advance();
                        char d3 = advance();
                        char d4 = peek();
                        c = (char)Integer.parseInt(d1 + d2 + d3 + String.valueOf(d4));
                }

                advance();
            }

            builder.append(c);
        }

        if(!match(closing))
            throw error("Unterminated string");

        String substring = builder.toString();
        addToken(TokenType.STRING_LITERAL, substring);
    }

    private void scanIdentifier() {
        while(Character.isLetterOrDigit(peek()) || peek() == '_') advance();

        String id = source.substring(start, current);
        TokenType type = keywords.getOrDefault(id, TokenType.IDENTIFIER);

        addToken(type);
    }

    private boolean match(char expected) {
        if(peek() == expected) {
            advance();
            return true;
        }

        return false;
    }

    private boolean available() {
        return current < source.length();
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int offset) {
        if(current + offset >= source.length()) return '\0';
        return source.charAt(current + offset);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private LexerError error(String message) {
        return new LexerError("[Line " + line + " in " + context + "] " + message);
    }

}
