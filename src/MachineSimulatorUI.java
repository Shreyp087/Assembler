import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class MachineSimulatorUI {
    private static JTextField programFileField;
    private static JTextArea cacheArea, printerArea;
    private static JTextField[] gprFields = new JTextField[4];
    private static JTextField[] ixrFields = new JTextField[3];
    private static JTextField pcField, marField, mbrField, irField;
    private static int[] memory = new int[4096]; // Extended memory size
    private static int pc = 0, mar = 0, mbr = 0, ir = 0;
    private static boolean running = false;

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSCI 6461 Machine Simulator");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(173, 216, 230));
        frame.setLayout(null);

        JLabel title = new JLabel("CSCI 6461 Machine Simulator", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 20));
        title.setBounds(250, 10, 400, 30);
        frame.add(title);

        setupRegisterFields(frame);
        setupMemoryAndConsole(frame);
        setupButtons(frame);
        frame.setVisible(true);
    }

    private static void setupRegisterFields(JFrame frame) {
        JLabel gprLabel = new JLabel("GPR");
        gprLabel.setBounds(30, 50, 50, 20);
        frame.add(gprLabel);
        for (int i = 0; i < 4; i++) {
            gprFields[i] = new JTextField("0");
            gprFields[i].setBounds(30, 80 + (i * 30), 100, 20);
            frame.add(gprFields[i]);
        }

        JLabel ixrLabel = new JLabel("IXR");
        ixrLabel.setBounds(160, 50, 50, 20);
        frame.add(ixrLabel);
        for (int i = 0; i < 3; i++) {
            ixrFields[i] = new JTextField("0");
            ixrFields[i].setBounds(160, 80 + (i * 30), 100, 20);
            frame.add(ixrFields[i]);
        }

        pcField = createRegisterField(frame, "PC", 300, 80);
        marField = createRegisterField(frame, "MAR", 300, 130);
        mbrField = createRegisterField(frame, "MBR", 300, 180);
        irField = createRegisterField(frame, "IR", 300, 230);
    }

    private static JTextField createRegisterField(JFrame frame, String label, int x, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(x, y - 30, 50, 20);
        frame.add(lbl);
        JTextField field = new JTextField("0");
        field.setBounds(x, y, 100, 20);
        frame.add(field);
        return field;
    }

    private static void setupMemoryAndConsole(JFrame frame) {
        JLabel cacheLabel = new JLabel("Cache Content");
        cacheLabel.setBounds(650, 50, 100, 20);
        frame.add(cacheLabel);
        cacheArea = new JTextArea();
        JScrollPane cacheScroll = new JScrollPane(cacheArea);
        cacheScroll.setBounds(650, 80, 200, 100);
        frame.add(cacheScroll);

        JLabel printerLabel = new JLabel("Printer");
        printerLabel.setBounds(650, 190, 100, 20);
        frame.add(printerLabel);
        printerArea = new JTextArea();
        JScrollPane printerScroll = new JScrollPane(printerArea);
        printerScroll.setBounds(650, 210, 200, 60);
        frame.add(printerScroll);

        JLabel programFileLabel = new JLabel("Program File:");
        programFileLabel.setBounds(650, 280, 100, 20);
        frame.add(programFileLabel);

        programFileField = new JTextField();
        programFileField.setBounds(650, 300, 200, 25);
        programFileField.setEditable(false);
        frame.add(programFileField);
    }

    private static void setupButtons(JFrame frame) {
        JButton loadBtn = new JButton("Load");
        loadBtn.setBounds(100, 350, 80, 30);
        frame.add(loadBtn);
        loadBtn.addActionListener(e -> loadROMFile());

        JButton stepBtn = new JButton("Step");
        stepBtn.setBounds(200, 350, 80, 30);
        frame.add(stepBtn);
        stepBtn.addActionListener(e -> stepInstruction());

        JButton haltBtn = new JButton("Halt");
        haltBtn.setBounds(300, 350, 80, 30);
        frame.add(haltBtn);
        haltBtn.addActionListener(e -> running = false);

        JButton iplBtn = new JButton("IPL");
        iplBtn.setBounds(400, 350, 80, 30);
        frame.add(iplBtn);
        iplBtn.addActionListener(e -> initializeIPL());

    

        JButton runBtn = new JButton("Run");
        runBtn.setBounds(500, 350, 80, 30);
        frame.add(runBtn);
        runBtn.addActionListener(e -> runProgram());

        JButton storeBtn = new JButton("Store");
        // Move it to a new position, e.g. (600, 350)
        storeBtn.setBounds(600, 350, 80, 30);
        frame.add(storeBtn);
        storeBtn.addActionListener(e -> storeToFile());
    }

    private static void loadROMFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        int returnValue = fileChooser.showOpenDialog(null);
        
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            programFileField.setText(selectedFile.getAbsolutePath());
    
            // ✅ Clear memory before loading
            for (int i = 0; i < memory.length; i++) {
                memory[i] = 0;
            }
    
            readFile(selectedFile);
            
            // ✅ Set PC to 010 (octal 10) to match IPL behavior
            pc = 010;
            mar = pc;
            mbr = memory[pc];
            ir = mbr;
    
            updateCacheDisplay();
            updateUI();
            printerArea.append("ROM file loaded into memory successfully. PC set to start at " + Integer.toOctalString(pc) + "\n");
        }
    }    
    

    private static void readFile(File file) {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 2) {
                        try {
                            int address = Integer.parseInt(parts[0], 8);
                            int value = Integer.parseInt(parts[1], 8);
                            if (address >= 0 && address < memory.length) {  // Prevent out-of-bounds memory access
                                memory[address] = value;
                            }
                        } catch (NumberFormatException e) {
                            printerArea.append("Invalid memory format: " + line + "\n");
                        }
                    } else {
                        printerArea.append("Skipping invalid line: " + line + "\n");
                    }
                }
            }
            
            //  Update UI with new memory contents
            updateCacheDisplay();
            updateUI();
            printerArea.append("Memory successfully loaded from file.\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private static void initializeIPL() {
        loadROMFile();  // Load program into memory
        pc = 010;  // Set PC to Octal 10 (Start execution at memory address 10)
        mar = pc;
        mbr = memory[pc];
        ir = mbr;
    
        StringBuilder cacheContent = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {  
                cacheContent.append("Addr: ").append(Integer.toOctalString(i))
                        .append(" → ").append(Integer.toOctalString(memory[i])).append("\n");
            }
        }
        cacheArea.setText(cacheContent.toString());
    
        printerArea.append("IPL executed. PC set to first instruction at " + Integer.toOctalString(pc) + "\n");
        updateUI();
    }
    

    private static void stepInstruction() {
        if (pc >= memory.length) {
            running = false;
            return;
        }
    
        // Debugging Output Before Execution
        System.out.println("Before Execution: PC = " + Integer.toOctalString(pc));
    
        // Fetch instruction
        mar = pc;
        mbr = memory[mar];
        ir = mbr;
    
        // Extract opcode (first 6 bits)
        int opcode = (ir >> 10) & 0x3F;
    
        // Debugging Output to Printer
        printerArea.append("Executing instruction at PC " + Integer.toOctalString(pc) +
                " | MAR: " + Integer.toOctalString(mar) +
                " | MBR: " + Integer.toOctalString(mbr) +
                " | IR: " + Integer.toOctalString(ir) +
                " | Opcode: " + Integer.toOctalString(opcode) + "\n");
    
        executeInstruction(ir);  // Execute instruction
    
        // 🔹 Move PC Forward **ONLY IF NOT HALTED**
        if (!pcField.getText().equals("HALT")) {  
            pc++;  // Move to the next instruction **only if it's not HALT**
            pcField.setText(Integer.toOctalString(pc));  // Update PC in UI
        }
    
        // 🔹 Debugging Output After Execution
        System.out.println("After Execution: PC = " + Integer.toOctalString(pc));
    
        updateUI();  // Ensure UI reflects the correct PC value
    }
     
    private static void runProgram() {
        new Thread(() -> {
            running = true;
            while (running && pc < memory.length) {
                // Update the UI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    stepInstruction();
                });
                try {
                    // Delay between instructions (500 milliseconds here)
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    printerArea.append("Run interrupted.\n");
                    break;
                }
            }
        }).start();
    }
    
    private static void storeToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Loop through memory and write non-zero cells
                for (int i = 0; i < memory.length; i++) {
                    if (memory[i] != 0) {
                        writer.printf("%06o %06o%n", i, memory[i]);
                    }
                }
                printerArea.append("Memory stored successfully to " + file.getAbsolutePath() + "\n");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error storing file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private static void executeInstruction(int instruction) {
        int opcode = (instruction >> 10) & 0x3F;  // Extract opcode
        int address = instruction & 0x3FF;  // Extract address part (last 10 bits)
        int register = (instruction >> 8) & 0x3;  // Extract register number
    
        // 🔹 Debugging Before Execution
        System.out.println("Before Execution: PC = " + Integer.toOctalString(pc));
    
        switch (opcode) {
            case 0x00:  // HALT
                running = false;
                printerArea.append("Program Halted.\n");
                pcField.setText("HALT");  //  Show HALT in UI
                return;  // Stop execution, do not increment PC
                
            case 0x21:  // LDR (Load Register)
                gprFields[register].setText(Integer.toOctalString(memory[address]));
                printerArea.append("LDR executed: R" + register + " ← Memory[" + Integer.toOctalString(address) + "]\n");
                break;
    
            case 0x22:  // STR (Store Register)
                memory[address] = Integer.parseInt(gprFields[register].getText(), 8);
                printerArea.append("STR executed: Memory[" + Integer.toOctalString(address) + "] ← R" + register + "\n");
                break;
    
            default:
                printerArea.append("Unknown Instruction: " + Integer.toOctalString(instruction) + "\n");
                break;
        }
    
        // //  Move PC Forward **ONLY IF NOT HALTED**
        // if (!pcField.getText().equals("HALT")) {
        //     pc++;  //  Increment PC if the program is still running
        //     pcField.setText(Integer.toOctalString(pc));  //  Update the PC field in the UI
        // }
    
        //  Debugging After Execution
        System.out.println("After Execution: PC = " + Integer.toOctalString(pc));
    
        updateUI();  //  Update UI to reflect the new PC value
    }
    
    private static void updateCacheDisplay() {
        StringBuilder cacheContent = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                cacheContent.append("Addr: ").append(Integer.toOctalString(i))
                            .append(" → ").append(Integer.toOctalString(memory[i])).append("\n");
            }
        }
        cacheArea.setText(cacheContent.toString());
    }
    
    private static void updateUI() {
        pcField.setText(Integer.toOctalString(pc));
        marField.setText(Integer.toOctalString(mar));
        mbrField.setText(Integer.toOctalString(mbr));
        irField.setText(Integer.toOctalString(ir));
    }
}
