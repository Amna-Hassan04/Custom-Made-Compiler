package misc;
import java.io.*;
import java.util.*;
import java.util.regex.*;

enum TokenType {
    KEYWORD,
    IDENTIFIER,
    LITERAL,   // Numbers, characters, strings
    OPERATOR,
    SEPARATOR,
    COMMENT,   // // or /* ... */
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

public class WppScanner {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "int", "float", "double", "char", "string", "bool", "void"
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
        try (BufferedReader reader = new BufferedReader(new FileReader("input.wpp"));
             PrintWriter writer = new PrintWriter(new FileWriter("output.txt"))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, writer);
            }
            printSymbolTable(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processLine(String line, PrintWriter writer) {
        if (inMultiLineComment) {
            int endCommentPos = line.indexOf("*/");
            if (endCommentPos != -1) {
                inMultiLineComment = false;
                if (endCommentPos + 2 < line.length()) {
                    processLine(line.substring(endCommentPos + 2), writer);
                }
            }
            return;
        }

        int singleCommentPos = line.indexOf("//");
        if (singleCommentPos != -1) {
            line = line.substring(0, singleCommentPos);
        }

        int startCommentPos = line.indexOf("/*");
        if (startCommentPos != -1) {
            int endCommentPos = line.indexOf("*/", startCommentPos + 2);
            if (endCommentPos != -1) {
                processLine(line.substring(0, startCommentPos) + " " + line.substring(endCommentPos + 2), writer);
            } else {
                inMultiLineComment = true;
                processLine(line.substring(0, startCommentPos), writer);
            }
            return;
        }

        if (line.trim().isEmpty()) return;

        // Extract tokens using a new function that correctly identifies string literals
        List<String> tokens = extractTokens(line);

        for (String token : tokens) {
            TokenType type = getTokenType(token);
            writer.println(token + " -> " + type);

            switch (type) {
                case KEYWORD:
                    currentType = token;
                    expectingAssignment = false;
                    break;
                case IDENTIFIER:
                    if (currentType != null) {
                        symbolTable.put(token, new SymbolInfo(currentType, "(Not initialized)"));
                        lastIdentifier = token;
                        expectingAssignment = true;
                    }
                    break;
                case OPERATOR:
                    expectingAssignment = token.equals("=");
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

    private static List<String> extractTokens(String line) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile(
            "\"([^\"]*)\"|'([^']*)'|\\d+\\.\\d+|\\d+|[a-zA-Z_][a-zA-Z0-9_]*|==|!=|>=|<=|\\+\\+|--|[-+*/=;,(){}]"
        ).matcher(line);

        while (matcher.find()) {
            String token = matcher.group().trim();
            if (token.isEmpty()) continue;

            tokens.add(token);
        }
        return tokens;
    }

    private static TokenType getTokenType(String token) {
        if (token.startsWith("\"") && token.endsWith("\"") || token.startsWith("'") && token.endsWith("'")) {
            return TokenType.LITERAL;
        } else if (KEYWORDS.contains(token)) {
            return TokenType.KEYWORD;
        } else if (OPERATORS.contains(token)) {
            return TokenType.OPERATOR;
        } else if (SEPARATORS.contains(token)) {
            return TokenType.SEPARATOR;
        } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return TokenType.IDENTIFIER;
        } else if (token.matches("\\d+\\.\\d+") || token.matches("\\d+")) {
            return TokenType.LITERAL;
        } else {
            return TokenType.UNKNOWN;
        }
    }

    private static void printSymbolTable(PrintWriter writer) {
        writer.println("\nSymbol Table:\n------------------------\nIdentifier  Type  Value\n------------------------");
        
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            writer.printf("%-12s %-6s %s%n", 
                entry.getKey(), 
                entry.getValue().type, 
                entry.getValue().value);
        }
    }
}
