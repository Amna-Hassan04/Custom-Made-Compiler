import javax.swing.*;
import javax.swing.border.*;
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
        // Try Nimbus Look and Feel for a modern look
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
                if ("Nimbus".equals(info.getName())){
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // fallback to default
        }

        frame = new JFrame("W++ Lexical Scanner");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setLayout(new BorderLayout(5, 5));

        // ===== MENU BAR =====
        JMenuBar menuBar = new JMenuBar();
        JButton newFile = new JButton("New File");
        saveButton = new JButton("Save As");
        JButton exit = new JButton("Exit");

        // Action listeners for menu items
        newFile.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Feature under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        saveButton.addActionListener(e -> saveToFile());
        exit.addActionListener(e -> System.exit(0));

        menuBar.add(newFile);
        menuBar.add(saveButton);
        menuBar.add(exit);
        frame.setJMenuBar(menuBar);

        // ===== TOOLBAR =====
        JToolBar toolBar = new JToolBar();
        runButton = new JButton("Run");
        compileButton = new JButton("Compile");
        scanButton = new JButton("Scan");

        // Toolbar button actions
        compileButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Compile feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        runButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Run feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));
        scanButton.addActionListener(e -> scanText());

        toolBar.add(runButton);
        toolBar.add(compileButton);
        toolBar.add(scanButton);
        frame.add(toolBar, BorderLayout.NORTH);

        // ===== TOP: SOURCE CODE AREA (in a panel) =====
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Source Code", TitledBorder.LEFT, TitledBorder.TOP));
        inputTextArea = new JTextArea();
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputTextArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // ===== LEFT (Token List) =====
        JPanel tokenPanel = new JPanel(new BorderLayout());
        tokenPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Token List", TitledBorder.LEFT, TitledBorder.TOP));
        tokenTableArea = new JTextArea();
        tokenTableArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tokenTableArea.setEditable(false);
        tokenTableArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane tokenScrollPane = new JScrollPane(tokenTableArea);
        tokenPanel.add(tokenScrollPane, BorderLayout.CENTER);

        // ===== RIGHT (Symbol Table) =====
        JPanel symbolPanel = new JPanel(new BorderLayout());
        symbolPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Symbol Table", TitledBorder.LEFT, TitledBorder.TOP));
        symbolTableArea = new JTextArea();
        symbolTableArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        symbolTableArea.setEditable(false);
        symbolTableArea.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane symbolScrollPane = new JScrollPane(symbolTableArea);
        symbolPanel.add(symbolScrollPane, BorderLayout.CENTER);

        // ===== BOTTOM SPLIT: Token List (left) & Symbol Table (right) =====
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tokenPanel, symbolPanel);
        horizontalSplit.setResizeWeight(0.5); // Split half and half initially

        // ===== MAIN SPLIT: Source Code (top) & Token/Symbol (bottom) =====
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, horizontalSplit);
        verticalSplit.setDividerLocation(350);

        frame.add(verticalSplit, BorderLayout.CENTER);

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
        // 1) Grab input
        String input = inputTextArea.getText();
        tokenTableArea.setText("");
        symbolTableArea.setText("");

        // 2) Remove comments before tokenizing:
        //    - Single-line: // ...
        //    - Multi-line: /* ... */
        String commentRegex = "(?s)//[^\\r\\n]*|/\\*.*?\\*/";
        Pattern commentPattern = Pattern.compile(commentRegex);
        input = commentPattern.matcher(input).replaceAll("");

        // 3) Token regex for everything else
        String regex = "\\b(int|float|double|char|string|if|else)\\b" + // keywords
                       "|[+\\-*/%<>=!&|]{1,2}" +                       // operators
                       "|\\d+(\\.\\d+)?" +                             // numbers
                       "|'[^']'" +                                     // char literal
                       "|[\"\\u201C](.*?)[\"\\u201D]" +                // string literal
                       "|[a-zA-Z_][a-zA-Z0-9_]*" +                     // identifiers
                       "|[{}();,]";                                    // separators

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 4) Tokenize and categorize
        java.util.List<String> tokens = new ArrayList<>();
        while (matcher.find()) {
            String token = matcher.group();
            tokens.add(token);

            if (token.matches("\\b(int|float|double|char|string|if|else)\\b")) {
                tokenTableArea.append("[KEYWORD]: " + token + "\n");
            } else if (token.matches("[+\\-*/%<>=!&|]{1,2}")) {
                // Distinguish operator types
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

        // 5) Build the symbol table from declarations
        String currentType = null;
        String currentIdentifier = null;
        Map<String, SymbolInfo> symbolTable = new LinkedHashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t.matches("\\b(int|float|double|char|string)\\b")) {
                currentType = t;
            } else if (t.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                if (currentType != null) {
                    currentIdentifier = t;
                    if (!symbolTable.containsKey(currentIdentifier)) {
                        symbolTable.put(currentIdentifier, new SymbolInfo(currentType, "(Not initialized)"));
                    }
                }
            } else if (t.equals("=")) {
                if (currentIdentifier != null && i + 1 < tokens.size()) {
                    String next = tokens.get(i + 1);
                    String value = next;
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("\u201C") && value.endsWith("\u201D")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    symbolTable.put(currentIdentifier, new SymbolInfo(currentType, value));
                    i++; // skip literal token
                }
            } else if (t.equals(",")) {
                currentIdentifier = null;
            } else if (t.equals(";")) {
                currentType = null;
                currentIdentifier = null;
            }
        }

        symbolTableArea.append("Identifier\tType\tValue\n");
        symbolTableArea.append("------------------------\n");
        for (Map.Entry<String, SymbolInfo> entry : symbolTable.entrySet()) {
            symbolTableArea.append(entry.getKey() + "\t" 
                    + entry.getValue().type + "\t" 
                    + entry.getValue().value + "\n");
        }
    }

    /**
     * Saves ONLY the input text to a .txt file.
     */
    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.setDialogTitle("Save File as Text");

        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(inputTextArea.getText());
                JOptionPane.showMessageDialog(frame, "File saved successfully as " + fileToSave.getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScannerGUI::new);
    }
}
