import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private Lexer lexer;
    private Token currentToken;
    private List<String> variables;
    
    public Parser(Lexer lexer) throws IOException {
        this.lexer = lexer;
        this.currentToken = lexer.nextToken();
        this.variables = new ArrayList<>();
    }
    
    private void eat(String tokenType) throws IOException {
        if (currentToken != null && currentToken.type.equals(tokenType)) {
            System.out.println("Consuming token: " + currentToken);
            currentToken = lexer.nextToken();
        } else {
            throw new RuntimeException("Syntax error at line " + 
                (currentToken != null ? currentToken.line : "EOF") + 
                ": Expected " + tokenType + 
                ", found " + (currentToken != null ? currentToken.type + "(" + currentToken.value + ")" : "EOF"));
        }
    }
    
    public void parse() throws IOException {
        try {
            program();
            System.out.println("\n=== Parsing completed successfully! ===");
            System.out.println("Variables declared: " + variables);
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            throw e;
        }
    }
    
    private void program() throws IOException {
        while (currentToken != null) {
            statement();
        }
    }
    
    private void statement() throws IOException {
        if (currentToken == null) {
            return;
        }
        
        switch (currentToken.type) {
            case "INT":
                variableDeclaration();
                break;
            case "ID":
                assignment();
                break;
            case "IF":
                ifStatement();
                break;
            case "FOR":
                forLoop();
                break;
            case "WHILE":
                whileLoop();
                break;
            default:
                throw new RuntimeException("Syntax error at line " + currentToken.line + 
                    ": Unexpected token " + currentToken.type + "(" + currentToken.value + ")");
        }
    }
    
    private void variableDeclaration() throws IOException {
        eat("INT");
        String varName = currentToken.value;
        variables.add(varName);
        System.out.println("Declared variable: " + varName);
        eat("ID");
        
        if (currentToken != null && currentToken.type.equals("ASSIGN")) {
            eat("ASSIGN");
            expression();
        }
        
        eat("SEMICOLON");
    }
    
    private void assignment() throws IOException {
        String varName = currentToken.value;
        if (!variables.contains(varName)) {
            throw new RuntimeException("Undeclared variable at line " + currentToken.line + ": " + varName);
        }
        System.out.println("Assigning to variable: " + varName);
        eat("ID");
        eat("ASSIGN");
        expression();
        eat("SEMICOLON");
    }
    
    // Assignment without semicolon (for use in for loops)
    private void assignmentNoSemicolon() throws IOException {
        String varName = currentToken.value;
        if (!variables.contains(varName)) {
            throw new RuntimeException("Undeclared variable at line " + currentToken.line + ": " + varName);
        }
        System.out.println("Assigning to variable (no semicolon): " + varName);
        eat("ID");
        eat("ASSIGN");
        expression();
    }
    
    private void ifStatement() throws IOException {
        System.out.println("Parsing IF statement");
        eat("IF");
        eat("LPAREN");
        logicalExpression();
        eat("RPAREN");
        eat("BEGIN");
        
        while (currentToken != null && !currentToken.type.equals("END")) {
            statement();
        }
        
        eat("END");
        
        if (currentToken != null && currentToken.type.equals("ELSE")) {
            System.out.println("Parsing ELSE clause");
            eat("ELSE");
            eat("BEGIN");
            
            while (currentToken != null && !currentToken.type.equals("END")) {
                statement();
            }
            
            eat("END");
        }
    }
    
    private void forLoop() throws IOException {
        System.out.println("Parsing FOR loop");
        eat("FOR");
        eat("LPAREN");
        
        // First part: initialization (assignment with semicolon)
        assignment();
        
        // Second part: condition
        logicalExpression();
        eat("SEMICOLON");
        
        // Third part: increment (assignment without semicolon)
        assignmentNoSemicolon();
        
        eat("RPAREN");
        eat("BEGIN");
        
        while (currentToken != null && !currentToken.type.equals("END")) {
            statement();
        }
        
        eat("END");
    }
    
    private void whileLoop() throws IOException {
        System.out.println("Parsing WHILE loop");
        eat("WHILE");
        eat("LPAREN");
        logicalExpression();
        eat("RPAREN");
        eat("BEGIN");
        
        while (currentToken != null && !currentToken.type.equals("END")) {
            statement();
        }
        
        eat("END");
    }
    
    private void expression() throws IOException {
        term();
        while (currentToken != null && (currentToken.type.equals("PLUS") || currentToken.type.equals("MINUS"))) {
            if (currentToken.type.equals("PLUS")) {
                eat("PLUS");
            } else {
                eat("MINUS");
            }
            term();
        }
    }
    
    private void term() throws IOException {
        factor();
        while (currentToken != null && (currentToken.type.equals("MUL") || currentToken.type.equals("DIV"))) {
            if (currentToken.type.equals("MUL")) {
                eat("MUL");
            } else {
                eat("DIV");
            }
            factor();
        }
    }
    
    private void factor() throws IOException {
        if (currentToken == null) {
            throw new RuntimeException("Unexpected end of input in factor");
        }
        
        switch (currentToken.type) {
            case "NUMBER":
                eat("NUMBER");
                break;
            case "ID":
                String varName = currentToken.value;
                if (!variables.contains(varName)) {
                    throw new RuntimeException("Undeclared variable at line " + currentToken.line + ": " + varName);
                }
                eat("ID");
                break;
            case "LPAREN":
                eat("LPAREN");
                expression();
                eat("RPAREN");
                break;
            default:
                throw new RuntimeException("Syntax error at line " + currentToken.line + 
                    ": Unexpected token in factor: " + currentToken.type + "(" + currentToken.value + ")");
        }
    }
    
    private void logicalExpression() throws IOException {
        expression();
        
        if (currentToken != null) {
            switch (currentToken.type) {
                case "EQ":
                    eat("EQ");
                    expression();
                    break;
                case "NEQ":
                    eat("NEQ");
                    expression();
                    break;
                case "LT":
                    eat("LT");
                    expression();
                    break;
                case "GT":
                    eat("GT");
                    expression();
                    break;
                case "LEQ":
                    eat("LEQ");
                    expression();
                    break;
                case "GEQ":
                    eat("GEQ");
                    expression();
                    break;
                default:
                    // No comparison operator found - this is OK for some contexts
                    break;
            }
        }
    }
    
    public static void main(String[] args) {
        String[] testFiles = {"ornek1.tk", "ornek2.tk", "ornek3.tk", "ornek4.tk"};
        
        for (String filename : testFiles) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Testing file: " + filename);
            System.out.println("=".repeat(50));
            
            try {
                Lexer lexer = new Lexer(filename);
                Parser parser = new Parser(lexer);
                parser.parse();
                lexer.close();
            } catch (IOException e) {
                System.err.println("IO Error with " + filename + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Parse Error with " + filename + ": " + e.getMessage());
            }
        }
    }
}