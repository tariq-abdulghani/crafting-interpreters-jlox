package abdulghani.tariq.lox;
import java.util.ArrayList;
import java.util.List;
import static abdulghani.tariq.lox.TokenType.*;


/**
 statement      → exprStmt
 | printStmt
 | block ;

 block          → "{" declaration* "}" ;
 */

/**
 * assignment
 * **************************************************
 expression     → assignment ;
 assignment     → IDENTIFIER "=" assignment
 | equality ;
 */
/**
 * new modified grammar to allow declaring variables
 program        → declaration* EOF ;

 declaration    → varDecl
 | statement ;

 statement      → exprStmt
 | printStmt ;

 varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

 primary        → "true" | "false" | "nil"
 | NUMBER | STRING
 | "(" expression ")"
 | IDENTIFIER ; // new primary expression

 */

/**
 program        → statement* EOF ;

 statement      → exprStmt
 | printStmt ;

 exprStmt       → expression ";" ;
 printStmt      → "print" expression ";" ;

 */

/**

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

//    Expr parse() {
//        try {
//            return expression();
//        } catch (ParseError error) {
//            return null;
//        }
//    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
//        return equality();
        return assignment();

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

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
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

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

// statements parsing
    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null; // makes problem when u return null so it needs guard.
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }
}
