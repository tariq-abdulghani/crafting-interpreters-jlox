package abdulghani.tariq.lox;
import java.util.List;
import static abdulghani.tariq.lox.TokenType.*;

/**
 * our grammar
 expression     → equality ;
 equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 term           → factor ( ( "-" | "+" ) factor )* ;
 factor         → unary ( ( "/" | "*" ) unary )* ;
 unary          → ( "!" | "-" ) unary
 | primary ;
 primary        → NUMBER | STRING | "true" | "false" | "nil"
 | "(" expression ")" ;
 */

/**
 * Creates the AST for Lox grammar
 */
public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    private static class ParseError extends RuntimeException {}


    /**
     * Creates the AST for Lox grammar
     */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(EQUAL_EQUAL, BANG_EQUAL)){
            Token operator = previous();
            expr = new Expr.Binary(expr, operator, comparison());
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)){
            expr = new Expr.Binary(expr, previous(), term());
        }
        return expr;
    }

    private Expr term(){
        Expr expr = factor();
        while (match(PLUS, MINUS)){
            expr = new Expr.Binary(expr, previous(), factor());
        }
        return expr;
    }

    private Expr factor(){
        Expr expr = unary();
        while (match(SLASH, STAR)){
            expr = new Expr.Binary(expr, previous(), unary());
        }
        return expr;
    }

    /**
     unary          → ( "!" | "-" ) unary
     | primary ;
     */
    private Expr unary(){
        if(match(BANG, MINUS)){
            return new Expr.Unary(previous(), unary());
        }else{
            return primary();
        }
    }

    /**
     primary        → NUMBER | STRING | "true" | "false" | "nil"
     | "(" expression ")"
     */
    private Expr primary()  {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }


    // utils
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }
}
