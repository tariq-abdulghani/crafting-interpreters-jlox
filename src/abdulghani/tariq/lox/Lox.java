package abdulghani.tariq.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    private static final Interpreter interpreter = new Interpreter();


    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Lox bad parameters");
            System.exit(64);
        }else if( args.length == 1){
             runFile(args[0]);
        }else {
            runPrompt();
        }
    }

    private   static  void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65); // describe error
    }

    private   static  void runPrompt() throws IOException {
        System.out.println("lox v0.0.0");
        BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in));

        for(;;){
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false; // if error happened user shouldn't terminate session
        }
    }

    private   static  void run(String source){
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
//        for (Token token : tokens) {
//            System.out.println(token);
//        }

        Parser parser = new Parser(tokens);
//        Expr expression = parser.parse();
//
//        // Stop if there was a syntax error.
        if (hadError) return;
//
//        System.out.println(new AstPrinter().print(expression));
        List<Stmt> statements = parser.parse();
        interpreter.interpret(statements);


    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    // todo implement to be more user friendly
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
