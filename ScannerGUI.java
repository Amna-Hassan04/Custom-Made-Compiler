import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ScannerGUI {
    private JFrame frame;
    private JTextArea inputTextArea;
    private JTextArea symbolTableArea, tokenTableArea;
    private JButton runButton, compileButton, scanButton, saveButton;

    public ScannerGUI() {
        frame = new JFrame("Lexical Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // ===== MENU BAR =====
        JMenuBar menuBar = new JMenuBar();
        JButton newFile = new JButton("New File");
        saveButton = new JButton("Save As");
        JButton compile = new JButton("Compile");
        JButton run = new JButton("Run");
        JButton exit = new JButton("Exit");

        // Action listeners for menu items
        newFile.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Feature under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        compile.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Compile feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        run.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Run feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        exit.addActionListener(e -> System.exit(0));

        menuBar.add(newFile);
        menuBar.add(saveButton);
        menuBar.add(compile);
        menuBar.add(run);
        menuBar.add(exit);
        frame.setJMenuBar(menuBar);

        // ===== TOOLBAR =====
        JToolBar toolBar = new JToolBar();
        runButton = new JButton("Run");
        compileButton = new JButton("Compile");
        scanButton = new JButton("Scanner");

        // Action listeners for toolbar buttons
        compileButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Compile feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        runButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Run feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        // scanButton performs scanning
        scanButton.addActionListener(e -> scanText());

        toolBar.add(runButton);
        toolBar.add(compileButton);
        toolBar.add(scanButton);
        frame.add(toolBar, BorderLayout.NORTH);

        // ===== INPUT TEXT AREA =====
        inputTextArea = new JTextArea(8, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        frame.add(inputScrollPane, BorderLayout.CENTER);

        // ===== OUTPUT PANEL =====
        JPanel outputPanel = new JPanel(new GridLayout(1, 2));

        // Symbol Table Panel
        JPanel symbolPanel = new JPanel(new BorderLayout());
        JLabel symbolLabel = new JLabel("Symbol Table");
        symbolTableArea = new JTextArea(15, 20);
        symbolTableArea.setEditable(false);
        JScrollPane symbolScrollPane = new JScrollPane(symbolTableArea);
        symbolScrollPane.setPreferredSize(new Dimension(300, 200));
        symbolPanel.add(symbolLabel, BorderLayout.NORTH);
        symbolPanel.add(symbolScrollPane, BorderLayout.CENTER);

        // Token List Panel
        JPanel tokenPanel = new JPanel(new BorderLayout());
        JLabel tokenLabel = new JLabel("Token List");
        tokenTableArea = new JTextArea(15, 20);
        tokenTableArea.setEditable(false);
        JScrollPane tokenScrollPane = new JScrollPane(tokenTableArea);
        tokenScrollPane.setPreferredSize(new Dimension(300, 200));
        tokenPanel.add(tokenLabel, BorderLayout.NORTH);
        tokenPanel.add(tokenScrollPane, BorderLayout.CENTER);

        outputPanel.add(symbolPanel);
        outputPanel.add(tokenPanel);
        frame.add(outputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // A simple class to hold symbol information.
    static class SymbolInfo {
        String type;
        String value;
        SymbolInfo(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }

    private void scanText() {
        String input = inputTextArea.getText();
        tokenTableArea.setText("");
        symbolTableArea.setText("");

        // Regex explanation:
        // - Keywords: int, float, double, char, string, if, else
        // - Comments
        // - Operators (includes arithmetic, relational, etc.)
        // - Numbers
        // - Char literals (e.g., 'J')
        // - String literals (supports standard quotes "..." and smart quotes using Unicode escapes)
        // - Identifiers
        // - Separators: { } ( ) ; ,
        String regex = "\\b(int|float|double|char|string|if|else)\\b" +  // keywords
                       "|//.*" +                                          // comments
                       "|[+\\-*/%<>=!&|]{1,2}" +                           // operators
                       "|\\d+(\\.\\d+)?" +                                // numbers
                       "|'[^']'" +                                       // char literal
                       "|[\"\\u201C](.*?)[\"\\u201D]" +                  // string literal (standard or smart quotes)
                       "|[a-zA-Z_][a-zA-Z0-9_]*" +                        // identifiers
                       "|[{}();,]";                                      // separators

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // Use fully-qualified java.util.List to avoid ambiguity with java.awt.List
        java.util.List<String> tokens = new ArrayList<>();

        // Tokenize and output token categories
        while (matcher.find()) {
            String token = matcher.group();
            tokens.add(token);

            if (token.matches("\\b(int|float|double|char|string|if|else)\\b")) {
                tokenTableArea.append("[KEYWORD]: " + token + "\n");
            } else if (token.matches("//.*")) {
                tokenTableArea.append("[COMMENT]: " + token + "\n");
            } else if (token.matches("[+\\-*/%<>=!&|]{1,2}")) {
                // Distinguish operator types.
                if (token.equals("==") || token.equals("!=") || token.equals("<") ||
                    token.equals(">") || token.equals("<=") || token.equals(">=")) {
                    tokenTableArea.append("[RELATIONAL_OPERATOR]: " + token + "\n");
                } else if (token.equals("+") || token.equals("-") || token.equals("*") ||
                           token.equals("/") || token.equals("%")) {
                    tokenTableArea.append("[ARITHMETIC_OPERATOR]: " + token + "\n");
                } else if (token.equals("=")) {
                    tokenTableArea.append("[ASSIGNMENT_OPERATOR]: " + token + "\n");
                } else if (token.equals("&&") || token.equals("||")) {
                    tokenTableArea.append("[LOGICAL_OPERATOR]: " + token + "\n");
                } else {
                    tokenTableArea.append("[OPERATOR]: " + token + "\n");
                }
            } else if (token.matches("\\d+(\\.\\d+)?")) {
                tokenTableArea.append("[NUMBER]: " + token + "\n");
            } else if (token.matches("'[^']'")) {
                tokenTableArea.append("[CHAR_LITERAL]: " + token + "\n");
            } else if (token.matches("[\"\\u201C](.*?)[\"\\u201D]")) {
                tokenTableArea.append("[STRING_LITERAL]: " + token + "\n");
            } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                tokenTableArea.append("[IDENTIFIER]: " + token + "\n");
            } else if (token.matches("[{}();,]")) {
                tokenTableArea.append("[SEPARATOR]: " + token + "\n");
            } else {
                tokenTableArea.append("[UNKNOWN]: " + token + "\n");
            }
        }

        // Second pass: Process tokens to build the symbol table.
        // We look for declarations of the form:
        //   <type> <identifier> [= <literal>] {, <identifier> [= <literal>]} ;
        String currentType = null;
        String currentIdentifier = null;
        Map<String, SymbolInfo> symbolTable = new LinkedHashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            // If a type keyword is encountered, set currentType.
            if (t.matches("\\b(int|float|double|char|string)\\b")) {
                currentType = t;
            }
            // If an identifier is encountered and we're in a declaration context, record it.
            else if (t.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                if (currentType != null) {
                    currentIdentifier = t;
                    if (!symbolTable.containsKey(currentIdentifier)) {
                        symbolTable.put(currentIdentifier, new SymbolInfo(currentType, "(Not initialized)"));
                    }
                }
            }
            // If the assignment operator is found and we have an identifier, update its value.
            else if (t.equals("=")) {
                if (currentIdentifier != null && i + 1 < tokens.size()) {
                    String next = tokens.get(i + 1);
                    String value = next;
                    // For char and string literals, remove surrounding quotes
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("\u201C") && value.endsWith("\u201D")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    symbolTable.put(currentIdentifier, new SymbolInfo(currentType, value));
                    i++; // Skip the literal token.
                }
            }
            // Comma indicates another variable declaration in the same statement.
            else if (t.equals(",")) {
                currentIdentifier = null;
            }
            // Semicolon ends the declaration statement.
            else if (t.equals(";")) {
                currentType = null;
                currentIdentifier = null;
            }
        }

        // Print the symbol table.
        symbolTableArea.append("Identifier\tType\tValue\n");
        symbolTableArea.append("------------------------\n");
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            symbolTableArea.append(entry.getKey() + "\t" + entry.getValue().type + "\t" + entry.getValue().value + "\n");
        }
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        // Set file filter to only allow text files.
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.setDialogTitle("Save File as Text");

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Append ".txt" if not present.
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }
            try (FileWriter writer = new FileWriter(fileToSave)) {
                // Save both the input text and the scanner outputs.
                writer.write("Input Source Code:\n");
                writer.write(inputTextArea.getText() + "\n\n");
                writer.write("Token List:\n");
                writer.write(tokenTableArea.getText() + "\n");
                writer.write("Symbol Table:\n");
                writer.write(symbolTableArea.getText());
                JOptionPane.showMessageDialog(frame, "File saved successfully as " + fileToSave.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new ScannerGUI();
    }
}
