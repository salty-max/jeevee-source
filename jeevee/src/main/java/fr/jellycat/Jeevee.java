package fr.jellycat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class Jeevee {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static String ANSI_BOLD = "\u001B[1m";
    static String ANSI_RED = "\u001B[31m";
    static String ANSI_BLUE = "\u001B[34m";
    static String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.print("Usage: jeevee [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            hadError = false;

            System.out.print("> ");
            Scanner scanner = new Scanner(reader.readLine());
            List<Token> tokens = scanner.scanTokens();

            Parser parser = new Parser(tokens);
            Object syntax = parser.parseREPL();

            if (hadError)
                continue;

            if (syntax instanceof List) {
                interpreter.interpret((List<Stmt>) syntax);
            } else if (syntax instanceof Expr) {
                String result = interpreter.interpret((Expr) syntax);
                if (result != null) {
                    Jeevee.log("= " + result);
                }
            }
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError)
            return;

        interpreter.interpret(statements);

    }

    static void error(int line, int column, String message) {
        report(line, column, "", message);
    }

    private static void report(int line, int column, String where, String message) {
        System.err.println(
                ANSI_BOLD + ANSI_RED + "Error" + where + " (" + line + ":" + column + "): " + message + ANSI_RESET);
        hadError = true;
    }

    static void error(Token token, int column, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, column, " at end", message);
        } else {
            report(token.line, column, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(ANSI_BOLD + ANSI_RED +
                "Runtime error -> (" + error.token.line + ":" + error.token.column + ")\n" + error.getMessage()
                + ANSI_RESET);
        hadRuntimeError = true;
    }

    static void log(String value) {
        System.out.println(ANSI_BOLD + ANSI_BLUE + value + ANSI_RESET);
    }
}
