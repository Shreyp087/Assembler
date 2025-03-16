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

    // ---------------------------
    // NEW: Simple Cache (16 lines)
    // ---------------------------
    private static Cache cache = new Cache(16);

    public static void main(String[] args) {
        JFrame frame = new JFrame("CSCI 6461 Machine Simulator");
        frame.setSize(1000, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(173, 216, 230));
        frame.setLayout(null);

        JLabel title = new JLabel("CSCI 6461 Machine Simulator", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 20));
        title.setBounds(300, 10, 400, 30);
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
        cacheLabel.setBounds(700, 50, 100, 20);
        frame.add(cacheLabel);
        cacheArea = new JTextArea();
        JScrollPane cacheScroll = new JScrollPane(cacheArea);
        cacheScroll.setBounds(700, 80, 200, 100);
        frame.add(cacheScroll);

        JLabel printerLabel = new JLabel("Printer");
        printerLabel.setBounds(700, 190, 100, 20);
        frame.add(printerLabel);
        printerArea = new JTextArea();
        JScrollPane printerScroll = new JScrollPane(printerArea);
        printerScroll.setBounds(700, 210, 200, 60);
        frame.add(printerScroll);

        JLabel programFileLabel = new JLabel("Program File:");
        programFileLabel.setBounds(700, 280, 100, 20);
        frame.add(programFileLabel);

        programFileField = new JTextField();
        programFileField.setBounds(700, 300, 200, 25);
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
        haltBtn.addActionListener(e -> {
            running = false;
            printerArea.append("Program Halted by User.\n");
        });
        
        JButton iplBtn = new JButton("IPL");
        iplBtn.setBounds(400, 350, 80, 30);
        frame.add(iplBtn);
        iplBtn.addActionListener(e -> initializeIPL());

        JButton runBtn = new JButton("Run");
        runBtn.setBounds(500, 350, 80, 30);
        frame.add(runBtn);
        runBtn.addActionListener(e -> runProgram());

        JButton storeBtn = new JButton("Store");
        // Moved to 600, 350
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
    
            // ✅ Set PC to 010 (Octal 10) to match IPL behavior
            pc = 010;
            mar = pc;
            mbr = memory[pc];
            ir = mbr;
    
            updateCacheDisplay();
            updateUI();
    
            // ✅ Ensure the printer output also uses octal formatting
            printerArea.append("ROM file loaded into memory successfully. PC set to start at "
                               + String.format("%06o", pc) + "\n");
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
                            if (address >= 0 && address < memory.length) {  
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
            
            updateCacheDisplay();
            updateUI();
            printerArea.append("Memory successfully loaded from file.\n");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + e.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void initializeIPL() {
        running = false; // Stop any previously running execution
        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0; // Clear memory
        }
    
        loadROMFile(); // Load program into memory
    
        // Set PC to 010 (Octal 10)
        pc = 010; 
        if (pc >= memory.length) {
            printerArea.append("Error: PC out of memory range!\n");
            return;
        }
    
        // Reset all registers before execution
        for (JTextField field : gprFields) field.setText("0");
        for (JTextField field : ixrFields) field.setText("0");
    
        mar = pc;
        mbr = memory[pc];
        ir = mbr;
    
        // Show non-zero memory in cache area (existing logic)
        StringBuilder cacheContent = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                cacheContent.append("Addr: ")
                            .append(String.format("%06o", i))
                            .append(" → ")
                            .append(String.format("%06o", memory[i]))
                            .append("\n");
            }
        }
        cacheArea.setText(cacheContent.toString());
    
        printerArea.append("IPL executed. PC set to first instruction at "
                           + String.format("%06o", pc) + "\n");
        updateUI();
    }

    private static void stepInstruction() {
        if (pc >= memory.length) {
            running = false;
            return;
        }
    
        // Debugging Output Before Execution
        System.out.println("Before Execution: PC = " + Integer.toOctalString(pc));
    
        // Fetch instruction (still from memory directly, no changes)
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
    
        // Move PC if not halted
        if (!pcField.getText().equals("HALT")) {
            pc++;
            pcField.setText(Integer.toOctalString(pc));
        }
    
        // Debugging Output After Execution
        System.out.println("After Execution: PC = " + Integer.toOctalString(pc));
    
        updateUI();
    }

    private static void runProgram() {
        new Thread(() -> {
            running = true;
            while (running && pc < memory.length) {
                SwingUtilities.invokeLater(() -> stepInstruction());
                try {
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
                JOptionPane.showMessageDialog(null, "Error storing file: " + e.getMessage(),
                                              "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void executeInstruction(int instruction) {
        int opcode   = (instruction >> 10) & 0x3F;  // Extract opcode
        int address  = instruction & 0x3FF;         // Last 10 bits
        int register = (instruction >> 8) & 0x3;    // Bits [9..8]
    
        // Debugging Output Before Execution
        System.out.println("Before Execution: PC = " + String.format("%06o", pc));
    
        switch (opcode) {
            case 0x00:  // HALT
                running = false;
                printerArea.append("Program Halted.\n");
                pcField.setText("HALT");
                return;
    
            case 0x21:  // LDR (Load Register)
                // NEW: read from cache instead of memory
                int data = cache.read(address);
                gprFields[register].setText(String.format("%06o", data));
                printerArea.append("LDR executed: R" + register + " ← Memory[" 
                                   + String.format("%06o", address) + "] via Cache\n");
                break;
    
            case 0x22:  // STR (Store Register)
                // NEW: write to cache (which writes through to memory)
                int value = Integer.parseInt(gprFields[register].getText(), 8);
                cache.write(address, value);
                printerArea.append("STR executed: Memory[" + String.format("%06o", address) 
                                   + "] ← R" + register + " via Cache\n");
                break;
    
            default:
                printerArea.append("Unknown Instruction: " 
                                   + String.format("%06o", instruction) + "\n");
                break;
        }
    
        updateUI();
    }
      
    private static void updateCacheDisplay() {
        // Original memory display logic (unchanged)
        StringBuilder cacheContent = new StringBuilder();
        for (int i = 0; i < memory.length; i++) {
            if (memory[i] != 0) {
                cacheContent.append("Addr: ")
                            .append(String.format("%06o", i))
                            .append(" → ")
                            .append(String.format("%06o", memory[i]))
                            .append("\n");
            }
        }
        cacheArea.setText(cacheContent.toString());

        // NEW: Append the actual cache lines after the memory listing
        cacheArea.append("\n=== Cache Lines ===\n");
        cacheArea.append(cache.getCacheContent());
    }
    
    
    private static void updateUI() {
        pcField.setText(String.format("%06o", pc));
        marField.setText(String.format("%06o", mar));
        mbrField.setText(String.format("%06o", mbr));
        irField.setText(String.format("%06o", ir));
    }
    
    // --------------------------------
    // NEW: Classes for Fully Associative Cache
    // --------------------------------
    static class CacheLine {
        boolean valid;
        int tag;
        int data;

        public CacheLine() {
            valid = false;
            tag = -1;
            data = 0;
        }
    }

    static class Cache {
        private CacheLine[] lines;
        private int nextReplaceIndex; // FIFO pointer

        public Cache(int numberOfLines) {
            lines = new CacheLine[numberOfLines];
            for (int i = 0; i < numberOfLines; i++) {
                lines[i] = new CacheLine();
            }
            nextReplaceIndex = 0;
        }

        // Read: check if address is in any valid line
        // On miss, load from memory, replace line at nextReplaceIndex
        public int read(int address) {
            for (CacheLine line : lines) {
                if (line.valid && line.tag == address) {
                    // HIT
                    printerArea.append("Cache HIT for address " + String.format("%06o", address) + "\n");
                    return line.data;
                }
            }
            // MISS
            printerArea.append("Cache MISS for address " + String.format("%06o", address) + "\n");
            int dataFromMem = memory[address];
            CacheLine replace = lines[nextReplaceIndex];
            replace.valid = true;
            replace.tag = address;
            replace.data = dataFromMem;
            nextReplaceIndex = (nextReplaceIndex + 1) % lines.length;
            return dataFromMem;
        }

        // Write: write-through to memory, update cache if line is present; otherwise replace FIFO line
        public void write(int address, int value) {
            memory[address] = value; // write-through
            for (CacheLine line : lines) {
                if (line.valid && line.tag == address) {
                    line.data = value;
                    printerArea.append("Cache HIT on write for address " 
                                       + String.format("%06o", address) + "\n");
                    return;
                }
            }
            // MISS
            printerArea.append("Cache MISS on write for address " 
                               + String.format("%06o", address) + "\n");
            CacheLine replace = lines[nextReplaceIndex];
            replace.valid = true;
            replace.tag = address;
            replace.data = value;
            nextReplaceIndex = (nextReplaceIndex + 1) % lines.length;
        }

        // Return a text representation of all cache lines
        public String getCacheContent() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lines.length; i++) {
                CacheLine line = lines[i];
                sb.append("Line ").append(i).append(": ");
                if (line.valid) {
                    sb.append("Addr=").append(String.format("%06o", line.tag))
                      .append(", Data=").append(String.format("%06o", line.data));
                } else {
                    sb.append("Empty");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }
}
