import java.io.*;
import java.util.*;

class Token {
    String type, value;
    
    Token(String type, String value) {
        this.type = type;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return String.format("%-20s %s", value, type);
    }
}

class Symbol {
    String identifier, type, value;
    
    Symbol(String identifier, String type, String value) {
        this.identifier = identifier;
        this.type = type;
        this.value = value.isEmpty() ? "(Not initialized)" : value;
    }
    
    @Override
    public String toString() {
        return String.format("%-10s %-10s %-15s", identifier, type, value);
    }
}

class SymbolTable {
    LinkedHashMap<String, Symbol> symbols = new LinkedHashMap<>();
    
    void insert(String identifier, String type, String value) {
        if (symbols.containsKey(identifier)) {
            Symbol sym = symbols.get(identifier);
            if (!value.isEmpty()) {
                // Update the value.
                sym.value = value;
                // Update the type if a new (non-empty) type is provided and it differs.
                if (!type.isEmpty() && !sym.type.equals(type)) {
                    sym.type = type;
                }
            }
        } else {
            symbols.put(identifier, new Symbol(identifier, type, value));
        }
    }
    
    void print(PrintWriter writer) {
        writer.println("\nSymbol Table:");
        writer.println("------------------------");
        writer.println("Identifier Type Value");
        writer.println("------------------------");
        System.out.println("\nSymbol Table:");
        System.out.println("------------------------");
        System.out.println("Identifier Type Value");
        System.out.println("------------------------");
        for (Symbol sym : symbols.values()) {
            writer.println(sym);
            System.out.println(sym);
        }
    }
}

public class Lexer {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList("int", "float", "double", "char"));
    private static final Set<Character> SEPARATORS = new HashSet<>(Arrays.asList(';', ','));
    private static final Set<String> OPERATORS = new HashSet<>(Arrays.asList("=", "+", "-", "*", "/", "==", "!=", "+=", "-=", "*=", "/="));
    
    private final String input;
    private int pos = 0;
    private final List<Token> tokens = new ArrayList<>();
    private final SymbolTable symbolTable = new SymbolTable();
    private String currentDataType = "";
    private String lastIdentifier = "";
    
    public Lexer(String input) {
        this.input = input;
    }
    
    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }
    
    private void advance() {
        pos++;
    }
    
    private void skipSingleLineComment() {
        while (peek() != '\n' && peek() != '\0') {
            advance();
        }
    }
    
    private void skipMultiLineComment() {
        advance(); // Skip '*'
        while (peek() != '\0') {
            if (peek() == '*' && pos + 1 < input.length() && input.charAt(pos + 1) == '/') {
                advance();
                advance();
                return;
            }
            advance();
        }
    }
    private void scanIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            sb.append(peek());
            advance();
        }
        String token = sb.toString();
    
        if (KEYWORDS.contains(token)) {
            tokens.add(new Token("Keyword", token));
            // Set the current type for the declaration.
            currentDataType = token;
        } else {
            tokens.add(new Token("Identifier", token));
            lastIdentifier = token;
            if (!currentDataType.isEmpty()) {
                symbolTable.insert(token, currentDataType, "");
                System.out.println("DEBUG: Inserted identifier -> Identifier: " + token + ", Type: " + currentDataType);
            }
        }
    }
    
    
    private void scanNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;
        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') isFloat = true;
            sb.append(peek());
            advance();
        }
        String value = sb.toString();
        tokens.add(new Token(isFloat ? "Literal (Float)" : "Literal (Integer)", value));
        
        if (!lastIdentifier.isEmpty()) {
            symbolTable.insert(lastIdentifier, currentDataType, value);
            lastIdentifier = "";
            currentDataType = "";
            
        }
    }
    
    private void scanChar() {
        advance();
        String value = String.valueOf(peek());
        advance();
        advance();
        tokens.add(new Token("Literal (Char)", value));
        
        if (!lastIdentifier.isEmpty()) {
            symbolTable.insert(lastIdentifier, "char", value);
            lastIdentifier = "";

        }
    }
    
    private void scanOperator() {
        StringBuilder sb = new StringBuilder();
        sb.append(peek());
        advance();
        if (OPERATORS.contains(sb.toString() + peek())) {
            sb.append(peek());
            advance();
        }
        tokens.add(new Token("Operator", sb.toString()));
    }
    
    private void scanSeparator() {
        char sep = peek();
        tokens.add(new Token("Separator", String.valueOf(sep)));
        advance();
        // If we reach a semicolon, the declaration is finished.
        if (sep == ';') {
            currentDataType = "";
        }
    }
    
    
    private void scanString() {
        StringBuilder sb = new StringBuilder();
        advance(); // Skip opening quote
    
        while (peek() != '"' && peek() != '\0') {
            sb.append(peek());
            advance();
        }
        advance(); // Skip closing quote
    
        String value = sb.toString();
        tokens.add(new Token("Literal (String)", value));
    
        // 🛠 Fix: Force "string" type for this value
        System.out.println("DEBUG: Before inserting string -> lastIdentifier: " + lastIdentifier + ", currentDataType: " + currentDataType);
    
        if (!lastIdentifier.isEmpty()) {
            symbolTable.insert(lastIdentifier, "string", value); // ✅ Ensure "string" type
            System.out.println("DEBUG: Inserted string -> Identifier: " + lastIdentifier + ", Type: string, Value: " + value);
            lastIdentifier = ""; // Reset AFTER inserting
        }
    
        // Reset `currentDataType` to avoid affecting other tokens
        currentDataType = "";
    }
    
    
    public void tokenize() {
        while (pos < input.length()) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                advance();
            } else if (Character.isLetter(c) || c == '_') {
                scanIdentifierOrKeyword();
            } else if (Character.isDigit(c)) {
                scanNumber();
            } else if (SEPARATORS.contains(c)) {
                scanSeparator();
            } else if (OPERATORS.contains(String.valueOf(c))) {
                scanOperator();
            } else if (c == '\'') {
                scanChar();
            } else if (c == '"') {
                scanString();
            } else if (c == '/' && pos + 1 < input.length()) {
                if (input.charAt(pos + 1) == '/') {
                    skipSingleLineComment();
                } else if (input.charAt(pos + 1) == '*') {
                    skipMultiLineComment();
                } else {
                    scanOperator();
                }
            } else {
                advance();
            }
        }
    }
    
    public void writeOutput(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Token Type");
            writer.println("------------------------");
            System.out.println("Token Type");
            System.out.println("------------------------");
            for (Token token : tokens) {
                writer.println(token);
                System.out.println(token);
            }
            symbolTable.print(writer);
            
        }
    }
    
    public static void main(String[] args) throws IOException {
        StringBuilder code = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("input.wpp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                code.append(line).append("\n");
            }
        }
        
        Lexer lexer = new Lexer(code.toString());
        lexer.tokenize();
        lexer.writeOutput("output.txt");
    }
}
