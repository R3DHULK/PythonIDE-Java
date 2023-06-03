import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class PythonIDE extends JFrame implements ActionListener {

    private JTextArea textArea;
    private JFileChooser fileChooser;
    private String currentFile;
    private UndoManager undoManager;

    private JMenu viewMenu;
    private JMenuItem lightThemeMenuItem;
    private JMenuItem darkThemeMenuItem;

    public PythonIDE() {
        setTitle("Python IDE");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");

        newMenuItem.addActionListener(this);
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveAsMenuItem.addActionListener(this);

        fileMenu.add(newMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoMenuItem = new JMenuItem("Undo");
        JMenuItem redoMenuItem = new JMenuItem("Redo");

        undoMenuItem.addActionListener(this);
        redoMenuItem.addActionListener(this);

        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        menuBar.add(editMenu);

        JMenu runMenu = new JMenu("Code-Runner");
        JMenuItem runMenuItem = new JMenuItem("Run");
        JMenuItem developerMenuItem = new JMenuItem("Developer: Sumalya Chatterjee");
        developerMenuItem.addActionListener(this);
        runMenuItem.addActionListener(this);
        runMenu.add(runMenuItem);
        runMenu.add(developerMenuItem);
        menuBar.add(runMenu);

        viewMenu = new JMenu("View");
        lightThemeMenuItem = new JMenuItem("Light Theme");
        darkThemeMenuItem = new JMenuItem("Dark Theme");

        lightThemeMenuItem.addActionListener(this);
        darkThemeMenuItem.addActionListener(this);

        viewMenu.add(lightThemeMenuItem);
        viewMenu.add(darkThemeMenuItem);
        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Python Files", "py"));

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setTitle("*Python IDE");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setTitle("*Python IDE");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setTitle("*Python IDE");
            }
        });

        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("New")) {
            textArea.setText("");
            currentFile = null;
            setTitle("Python IDE");
        } else if (command.equals("Open")) {
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                currentFile = selectedFile.getAbsolutePath();
                openFile();
                setTitle("Python IDE - " + currentFile);
            }
        } else if (command.equals("Save")) {
            if (currentFile != null) {
                saveFile(currentFile);
                setTitle("Python IDE - " + currentFile);
            } else {
                saveFileAs();
            }
        } else if (command.equals("Save As")) {
            saveFileAs();
        } else if (command.equals("Run")) {
            runPythonCode();
        } else if (command.equals("Light Theme")) {
            setLightTheme();
        } else if (command.equals("Dark Theme")) {
            setDarkTheme();
        } else if (command.equals("Undo")) {
            undo();
        } else if (command.equals("Redo")) {
            redo();
        }
    }

    private void openFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(currentFile));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            textArea.setText(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(String filePath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(textArea.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileAs() {
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            saveFile(filePath);
            currentFile = filePath;
            setTitle("Python IDE - " + currentFile);
        }
    }

    private void runPythonCode() {
        String code = textArea.getText();

        try {
            // Save the code to a temporary file
            File tempFile = File.createTempFile("python_code", ".py");
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(code);
            writer.close();

            // Run the Python code using the system's Python interpreter
            Process process = Runtime.getRuntime().exec("python " + tempFile.getAbsolutePath());

            // Read the output of the Python code
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            StringBuilder errors = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errors.append(line).append("\n");
            }
            errorReader.close();

            // Display the output and errors in separate dialog boxes
            if (errors.length() > 0) {
                JOptionPane.showMessageDialog(null, errors.toString(), "Python Errors", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, output.toString(), "Python Output", JOptionPane.INFORMATION_MESSAGE);
            }

            // Delete the temporary file
            tempFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setLightTheme() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        viewMenu.setForeground(Color.BLACK);
        lightThemeMenuItem.setEnabled(false);
        darkThemeMenuItem.setEnabled(true);
    }

    private void setDarkTheme() {
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);
        viewMenu.setForeground(Color.WHITE);
        lightThemeMenuItem.setEnabled(true);
        darkThemeMenuItem.setEnabled(false);
    }

    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PythonIDE());
    }
}
