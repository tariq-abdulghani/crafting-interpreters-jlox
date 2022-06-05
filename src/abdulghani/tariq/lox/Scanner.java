package abdulghani.tariq.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static abdulghani.tariq.lox.TokenType.*;

public class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // where we are in the source
    private int start = 0; // start of current lexeme
    private int current = 0; // current char in lexeme
    private int line = 1; // line number

    private static final HashMap<String, TokenType> reservedWords;

    static {
//        AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
//                PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
        reservedWords = new HashMap<>();
        reservedWords.put(AND.name(), AND);
        reservedWords.put(CLASS.name(), CLASS);
        reservedWords.put(ELSE.name(), ELSE);
        reservedWords.put(FALSE.name(), FALSE);
        reservedWords.put(FUN.name(), FUN);
        reservedWords.put(FOR.name(), FOR);
        reservedWords.put(IF.name(), IF);
        reservedWords.put(NIL.name(), NIL);
        reservedWords.put(OR.name(), OR);
        reservedWords.put(PRINT.name(), PRINT);
        reservedWords.put(RETURN.name(), RETURN);
        reservedWords.put(SUPER.name(), SUPER);
        reservedWords.put(THIS.name(), THIS);
        reservedWords.put(TRUE.name(), TRUE);
        reservedWords.put(VAR.name(), VAR);
        reservedWords.put(WHILE.name(), WHILE);

    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        // not mandatory but make it clear where the end of file is
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
            //    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            // comments have // slash it needs to be considered
            case '/':
                if (match('/')) {
                    while (!isAtEnd() && source.charAt(current) != '\n') advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;
//                some logical operators
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '"':
                string();
                break;

            default:
                if (Character.isDigit(c)) {
                    number();
                } else if (Character.isAlphabetic(c)) {
                    identifier();
                } else Lox.error(line, "invalid input");
                break;
        }
    }

    // source traversal  utils
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Gets the next char in source
     *
     * @return char
     */
    private char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }

    /**
     * match current char to given one which is according to implementation is look ahead;
     *
     * @param c
     * @return
     */
    private boolean match(char c) {
        if (isAtEnd() || source.charAt(current) != c) return false;
        else {
            current++;
            return true;
        }
    }

    /**
     * get current unconsumed char
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    //    long lexeme
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);

    }

    //    number 1234 12.34
    private void number() {
        // while is digit advance
        while (Character.isDigit(peek())) {
            advance();
        }
        // if peek is dot advance
        if (peek() == '.') {
            advance();
            while (Character.isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    //    identifier
    private void identifier() {
        while (Character.isAlphabetic(peek()) || Character.isDigit(peek())) advance();
        String lexeme = source.substring(start, current);
        TokenType type =  reservedWords.get(lexeme.toUpperCase());
        addToken(type != null? type: IDENTIFIER, lexeme);
    }
}
