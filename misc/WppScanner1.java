package misc;
import java.io.*;
import java.util.*;
import java.util.regex.*;

enum TokenType {
    KEYWORD,
    IDENTIFIER,
    LITERAL,         // Numbers, characters, strings
    OPERATOR,
    SEPARATOR,
    COMMENT,         // // or /* ... */
    UNKNOWN;
}

class SymbolInfo {
    String type;
    String value;
    
    public SymbolInfo(String type, String value) {
        this.type = type;
        this.value = value;
    }
}

public class WppScanner1 {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "int", "float", "double", "char", "string"
    ));

    private static final Set<String> OPERATORS = new HashSet<>(Arrays.asList(
        "=", "+", "-", "*", "/", "++", "--", "==", "!=", ">=", "<="
    ));

    private static final Set<String> SEPARATORS = new HashSet<>(Arrays.asList(
        ";", ",", "(", ")", "{", "}"
    ));

    private static final Map<String, SymbolInfo> symbolTable = new HashMap<>();
    private static boolean inMultiLineComment = false;
    private static String currentType = null;
    private static String lastIdentifier = null;
    private static boolean expectingAssignment = false;

    public static void main(String[] args) {
        // For testing with multiple lines and comments
        String[] inputLines = {
            "int x; // This is a variable declaration",
            "float a, b, c; /* These are floating point variables */",
            "double y = 3.14159; // Value of pi",
            "/* This is a multi-line comment",
            "   that spans multiple lines */",
            "char initial = 'J';",
            "int z,p; // More integers",
            "string \"W++ source code\"; // This is a string literal"
        };
        
        PrintWriter writer = new PrintWriter(System.out, true);
        for (String line : inputLines) {
            processLine(line, writer);
        }
        printSymbolTable(writer);
    }

    private static void processLine(String line, PrintWriter writer) {
        // Handle multi-line comments that continue from previous lines
        if (inMultiLineComment) {
            int endCommentPos = line.indexOf("*/");
            if (endCommentPos != -1) {
                inMultiLineComment = false;
                // Process the rest of the line after */
                if (endCommentPos + 2 < line.length()) {
                    processLine(line.substring(endCommentPos + 2), writer);
                }
            }
            return;
        }

        // Handle single-line comments
        int singleCommentPos = line.indexOf("//");
        if (singleCommentPos != -1) {
            // Only process text before the comment
            line = line.substring(0, singleCommentPos);
        }

        // Handle multi-line comments that start in this line
        int startCommentPos = line.indexOf("/*");
        if (startCommentPos != -1) {
            int endCommentPos = line.indexOf("*/", startCommentPos + 2);
            if (endCommentPos != -1) {
                // Comment starts and ends on the same line
                String beforeComment = line.substring(0, startCommentPos);
                String afterComment = line.substring(endCommentPos + 2);
                processLine(beforeComment + " " + afterComment, writer);
            } else {
                // Comment starts but doesn't end on this line
                inMultiLineComment = true;
                // Only process text before the comment start
                if (startCommentPos > 0) {
                    processLine(line.substring(0, startCommentPos), writer);
                }
            }
            return;
        }

        // Process tokens if there's anything left after comment handling
        if (line.trim().isEmpty()) return;

        Matcher matcher = Pattern.compile(
            "\"([^\"])\"|'[^']'|\\d+\\.\\d+|\\d+|[a-zA-Z_][a-zA-Z0-9_]|[=+\\-*/;,(){}]"
        ).matcher(line);

        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.isEmpty()) continue;

            TokenType type = getTokenType(token);
            
            writer.println(token + " -> " + type);

            switch (type) {
                case KEYWORD:
                    if (KEYWORDS.contains(token)) {
                        currentType = token;
                        expectingAssignment = false;
                    }
                    break;
                    
                case IDENTIFIER:
                    if (currentType != null) {
                        symbolTable.put(token, new SymbolInfo(currentType, "(Not initialized)"));
                        lastIdentifier = token;
                        expectingAssignment = true;
                    }
                    break;
                    
                case OPERATOR:
                    if (token.equals("=")) {
                        expectingAssignment = true;
                    } else {
                        expectingAssignment = false;
                    }
                    break;
                    
                case LITERAL:
                    if (expectingAssignment && lastIdentifier != null && symbolTable.containsKey(lastIdentifier)) {
                        symbolTable.get(lastIdentifier).value = token;
                        expectingAssignment = false;
                    }
                    break;
                    
                case SEPARATOR:
                    if (token.equals(";")) {
                        currentType = null;
                        expectingAssignment = false;
                    }
                    break;
            }
        }
    }

    private static TokenType getTokenType(String token) {
        if (KEYWORDS.contains(token)) {
            return TokenType.KEYWORD;
        } else if (OPERATORS.contains(token)) {
            return TokenType.OPERATOR;
        } else if (SEPARATORS.contains(token)) {
            return TokenType.SEPARATOR;
        } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return TokenType.IDENTIFIER;
        } else if (token.matches("^'[^']'$") || token.matches("^\\d+\\.\\d+$") || token.matches("^\\d+$") ||
                  (token.startsWith("\"") && token.endsWith("\""))) {
            return TokenType.LITERAL;
        } else {
            return TokenType.UNKNOWN;
        }
    }

    private static void printSymbolTable(PrintWriter writer) {
        writer.println("\nSymbol Table:\n------------------------\nIdentifier Type Value\n------------------------");
        
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            writer.printf("%-10s %-6s %s%n", 
                entry.getKey(), 
                entry.getValue().type, 
                entry.getValue().value);
        }
    }
}