public class backup {
    
}

import javax.swing.*;
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

        // === MENU BAR ===
        JMenuBar menuBar = new JMenuBar();
        JButton newFile = new JButton("New File");
        saveButton = new JButton("Save As");
        JButton compile = new JButton("Compile");
        JButton run = new JButton("Run");
        JButton exit = new JButton("Exit");

        menuBar.add(newFile);
        menuBar.add(saveButton);
        menuBar.add(compile);
        menuBar.add(run);
        menuBar.add(exit);
        frame.setJMenuBar(menuBar);

        // === TOOLBAR ===
        JToolBar toolBar = new JToolBar();
        runButton = new JButton("Run");
        compileButton = new JButton("Compile");
        scanButton = new JButton("Scanner");

        toolBar.add(runButton);
        toolBar.add(compileButton);
        toolBar.add(scanButton);
        frame.add(toolBar, BorderLayout.NORTH);

        // === INPUT TEXT AREA ===
        inputTextArea = new JTextArea(8, 40);
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        frame.add(inputScrollPane, BorderLayout.CENTER);

        // === OUTPUT SECTION (LARGER HEIGHT) ===
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

        // Token Table Panel
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

        // === BUTTON ACTION LISTENERS ===
        scanButton.addActionListener(e -> scanText());

        saveButton.addActionListener(e -> saveToFile());

        compileButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, 
            "Compile feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));

        runButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, 
            "Run feature is under development.", "Info", JOptionPane.INFORMATION_MESSAGE));

        exit.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private void scanText() {
        String input = inputTextArea.getText();
        tokenTableArea.setText("");
        symbolTableArea.setText("");

        String regex = "\\b(if|else|int|float|return)\\b" +  
                       "|//.*" +                             
                       "|[+\\-*/%<>=!&]{1,2}|\\|{1,2}" +     
                       "|\\d+(\\.\\d+)?" +                   
                       "|[a-zA-Z_][a-zA-Z0-9_]*" +           
                       "|[{}();,]";                          

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        Set<String> symbolTable = new HashSet<>();

        while (matcher.find()) {
            String token = matcher.group();

            if (token.matches("\\b(if|else|int|float|return)\\b")) {
                tokenTableArea.append("[KEYWORD]: " + token + "\n");
            } else if (token.matches("//.*")) {
                tokenTableArea.append("[COMMENT]: " + token + "\n");
            } else if (token.matches("[+\\-*/%<>=!&|]{1,2}")) {
                tokenTableArea.append("[OPERATOR]: " + token + "\n");
            } else if (token.matches("\\d+(\\.\\d+)?")) {
                tokenTableArea.append("[NUMBER]: " + token + "\n");
            } else if (token.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                tokenTableArea.append("[IDENTIFIER]: " + token + "\n");
                symbolTable.add(token);
            } else if (token.matches("[{}();,]")) {
                tokenTableArea.append("[SYMBOL]: " + token + "\n");
                symbolTable.add(token);
            } else {
                tokenTableArea.append("[UNKNOWN]: " + token + "\n");
            }
        }

        for (String symbol : symbolTable) {
            symbolTableArea.append(symbol + "\n");
        }
    }

    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(inputTextArea.getText());
                JOptionPane.showMessageDialog(frame, "File saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        new ScannerGUI();
    }
}
