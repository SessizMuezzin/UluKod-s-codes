import java.util.*;
import java.io.*;

public class Lexer {
    private BufferedReader reader;
    private String currentLine;
    private int currentPos;
    private int lineNumber;
    
    private static final Map<String, String> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("olurMu", "IF");          
        KEYWORDS.put("budaMıDegil", "ELSE");   
        KEYWORDS.put("dönmeDolap", "FOR");
        KEYWORDS.put("çarkıFelek", "WHILE");
        KEYWORDS.put("basla", "BEGIN");
        KEYWORDS.put("bitir", "END");
        KEYWORDS.put("tam", "INT");
    }
    
    public Lexer(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        currentLine = "";
        currentPos = 0;
        lineNumber = 0;
    }
    
    public Token nextToken() throws IOException {
        while (true) {
            // If we've reached the end of current line, read next line
            if (currentLine == null || currentPos >= currentLine.length()) {
                currentLine = reader.readLine();
                lineNumber++;
                currentPos = 0;
                
                if (currentLine == null) {
                    return null;
                }
                
                currentLine = currentLine.trim();
                if (currentLine.isEmpty() || currentLine.startsWith("//")) {
                    continue;
                }
            }
            
            // Skip whitespace
            while (currentPos < currentLine.length() && Character.isWhitespace(currentLine.charAt(currentPos))) {
                currentPos++;
            }
            
            if (currentPos >= currentLine.length()) {
                continue;
            }
            
            char currentChar = currentLine.charAt(currentPos);
            
            if (Character.isDigit(currentChar)) {
                StringBuilder num = new StringBuilder();
                while (currentPos < currentLine.length() && Character.isDigit(currentLine.charAt(currentPos))) {
                    num.append(currentLine.charAt(currentPos));
                    currentPos++;
                }
                return new Token("NUMBER", num.toString(), lineNumber);
            }
            
            if (Character.isLetter(currentChar) || "ğüşıöçĞÜŞİÖÇ".indexOf(currentChar) != -1) {
                StringBuilder id = new StringBuilder();
                while (currentPos < currentLine.length() && 
                      (Character.isLetterOrDigit(currentLine.charAt(currentPos)) || 
                       "ğüşıöçĞÜŞİÖÇ".indexOf(currentLine.charAt(currentPos)) != -1)) {
                    id.append(currentLine.charAt(currentPos));
                    currentPos++;
                }
                
                String idStr = id.toString();
                if (KEYWORDS.containsKey(idStr)) {
                    return new Token(KEYWORDS.get(idStr), idStr, lineNumber);
                }
                return new Token("ID", idStr, lineNumber);
            }
            
            switch (currentChar) {
                case '=':
                    currentPos++;
                    if (currentPos < currentLine.length() && currentLine.charAt(currentPos) == '=') {
                        currentPos++;
                        return new Token("EQ", "==", lineNumber);
                    }
                    return new Token("ASSIGN", "=", lineNumber);
                case '!':
                    if (currentPos + 1 < currentLine.length() && currentLine.charAt(currentPos + 1) == '=') {
                        currentPos += 2;
                        return new Token("NEQ", "!=", lineNumber);
                    }
                    break;
                case '<':
                    currentPos++;
                    if (currentPos < currentLine.length() && currentLine.charAt(currentPos) == '=') {
                        currentPos++;
                        return new Token("LEQ", "<=", lineNumber);
                    }
                    return new Token("LT", "<", lineNumber);
                case '>':
                    currentPos++;
                    if (currentPos < currentLine.length() && currentLine.charAt(currentPos) == '=') {
                        currentPos++;
                        return new Token("GEQ", ">=", lineNumber);
                    }
                    return new Token("GT", ">", lineNumber);
                case '+':
                    currentPos++;
                    return new Token("PLUS", "+", lineNumber);
                case '-':
                    currentPos++;
                    return new Token("MINUS", "-", lineNumber);
                case '*':
                    currentPos++;
                    return new Token("MUL", "*", lineNumber);
                case '/':
                    currentPos++;
                    return new Token("DIV", "/", lineNumber);
                case '(':
                    currentPos++;
                    return new Token("LPAREN", "(", lineNumber);
                case ')':
                    currentPos++;
                    return new Token("RPAREN", ")", lineNumber);
                case ';':
                    currentPos++;
                    return new Token("SEMICOLON", ";", lineNumber);
                default:
                    currentPos++;
                    return new Token("UNKNOWN", Character.toString(currentChar), lineNumber);
            }
        }
    }
    
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}