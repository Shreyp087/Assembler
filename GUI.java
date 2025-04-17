//Main class that runs the GUI.

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.*;
import java.util.Arrays;
import java.util.Scanner;

public class GUI extends javax.swing.JFrame {

    // Global variables
    Core Processor;
    CacheMemory MemoryCache = new CacheMemory(16);
    InstructionFormatter strFormatter = new InstructionFormatter();
    int[] instructionArray = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    int[] default_PC_loc = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 };
    boolean run_check = false;

    int numberCount = 0;
    private static final int MAX_NUMBERS = 20;
    private static int[] enteredNumbers = new int[MAX_NUMBERS];

    // Function that runs the GUI and Processor
    public GUI() {
        initComponents();
        Processor = new Core();
        int[] tmp_val = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0 };
        Processor.setRegisterVals("PC", tmp_val);
        new Timer(delay, mainLoop).start();
    }

    int targetAddress = 2047; // Address for the 21st number
    int numbersBaseAddress = 1000; // Starting address for the 20 initial numbers
    int closestNumberAddress = 2000; // Address to store the closest number
    int minDifferenceAddress = 2001;

    void doMaths() {
        // Step 1: Store the 20 initial numbers in memory
        for (int i = 0; i < 20; i++) {
            // Convert the number to binary and store it as a 16-bit array
            Processor.setRegisterVals("MBR", strFormatter.intToBinaryArray(enteredNumbers[i]));
            // Store the binary array as an integer value in memory
            Processor.setMemoryVals(numbersBaseAddress + i, (Processor.getRegisterVals("MBR")));
        }

        // Step 2: Load the 21st (target) number into GPR0
        Processor.setRegisterVals("GPR0", Processor.getMemoryVals(targetAddress));

        // Initialize GPR1 for minimum difference (large initial value)
        Processor.setRegisterVals("GPR1", strFormatter.intToBinaryArray(0xFFFF)); // Max 16-bit value

        // Initialize closest number as 0 in GPR3
        Processor.setRegisterVals("GPR3", strFormatter.intToBinaryArray(0));

        // Step 3: Find the closest number
        for (int i = 0; i < 20; i++) {
            int currentAddress = numbersBaseAddress + i;

            // Load the current number from memory into GPR2
            Processor.setRegisterVals("GPR2", Processor.getMemoryVals(currentAddress));

            // Calculate the absolute difference
            int difference = Math.abs(strFormatter.binToInt(Processor.getRegisterVals("GPR0"))
                    - strFormatter.binToInt(Processor.getRegisterVals("GPR2")));

            // Store the difference temporarily in MBR for comparison
            Processor.setRegisterVals("MBR", strFormatter.intToBinaryArray(difference));

            // Check if this difference is smaller than the current minimum (in GPR1)
            if (strFormatter.binToInt(Processor.getRegisterVals("MBR")) < strFormatter
                    .binToInt(Processor.getRegisterVals("GPR1"))) {
                // Update GPR1 with the new minimum difference
                Processor.setRegisterVals("GPR1", Processor.getRegisterVals("MBR"));

                // Update GPR3 to hold the current closest number
                Processor.setRegisterVals("GPR3", Processor.getRegisterVals("GPR2"));
            }
        }

        // Step 4: Store results in memory
        Processor.setMemoryVals(closestNumberAddress, Processor.getRegisterVals("GPR3")); // Store as integer
        Processor.setMemoryVals(minDifferenceAddress, Processor.getRegisterVals("GPR1")); // Store as integer
    }

    // Method to find the closest number to the 21st number
    int findClosestNumber(int target, int[] numbers) {
        int closest = numbers[0];
        int minDifference = Math.abs(target - closest);

        for (int i = 1; i < numbers.length; i++) {
            int difference = Math.abs(target - numbers[i]);
            if (difference < minDifference) {
                minDifference = difference;
                closest = numbers[i];
            }
        }
        System.out.println(closest);
        return closest;
    }

    // Main loop of the program that runs every 500 millisecond
    int delay = 500;
    ActionListener mainLoop = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {

            // Update the Registers for display
            updateRegisters();

            // Check if machine is set to run
            if (run_check) {
                String instructionAddress = MemoryCache.fetchVal(Arrays.toString(Processor.getRegisterVals("PC")));
                System.out.println(instructionAddress);

                int[] instruction_address = Processor.getRegisterVals("PC");
                int int_instruction_address = strFormatter.binToInt(instruction_address);
                Processor.setRegisterVals("IR", Processor.getMemoryVals(int_instruction_address));

                if (instructionAddress == null) {
                    MemoryCacheTextField.setText("Cache Missed !!");
                    System.out.println("Cache Missed !!");
                    MemoryCache.insertVal(Arrays.toString(Processor.getRegisterVals("PC")),
                            Arrays.toString(Processor.getRegisterVals("IR")));
                    StringBuilder textContent = new StringBuilder();
                    for (String key : MemoryCache.cache.keySet()) {
                        String value = MemoryCache.cache.get(key);
                        textContent.append("MAR: ").append(key).append(", MBR: ").append(value).append("; ");
                    }

                    // Set the concatenated string as the text in the JTextField
                    MemoryCacheValueTextField.setText(textContent.toString());

                    System.out.println(
                            Arrays.toString(Processor.getRegisterVals("PC"))
                                    + Arrays.toString(Processor.getRegisterVals("IR")));
                } else {
                    MemoryCacheTextField.setText("Cache Hit!");
                    System.out.println("Cache Hit!");
                }
                Processor.execute("single");
                // If running, check if the halt flag is raised
                if (Processor.getRegisterVals("HLT")[0] == 1) {
                    run_check = false;
                    int[] msg = new int[] { 0 };
                    Processor.setRegisterVals("HLT", msg);
                }
            }
        }
    };

    // Variables declaration
    private javax.swing.JTextField CCTextField;
    private javax.swing.JTextField GPR0TextField;
    private javax.swing.JTextField GPR1TextField;
    private javax.swing.JTextField GPR2TextField;
    private javax.swing.JTextField GPR3TextField;
    private javax.swing.JTextField IRTextField;
    private javax.swing.JTextField IXR1TextField;
    private javax.swing.JTextField IXR2TextField;
    private javax.swing.JTextField IXR3TextField;
    private javax.swing.JTextField MARTextField;
    private javax.swing.JTextField MBRTextField;
    private javax.swing.JTextField MFRTextField;
    private javax.swing.JTextField Mem0RowTextField;
    private javax.swing.JTextField Mem0ValueTextField;
    private javax.swing.JTextArea MemoryCacheValueTextField;
    private javax.swing.JTextArea KeyBoardArea;
    private javax.swing.JTextArea PrinterArea;
    private javax.swing.JTextField MemoryCacheTextField;

    private javax.swing.JTextField PCTextfField;
    private javax.swing.JPanel column1;
    private javax.swing.JPanel column2;

    // Function to create the various buttons and text fields
    private void initComponents() {

        column1 = new javax.swing.JPanel();
        column2 = new javax.swing.JPanel();
        PCTextfField = new javax.swing.JTextField();
        MARTextField = new javax.swing.JTextField();
        MBRTextField = new javax.swing.JTextField();
        IRTextField = new javax.swing.JTextField();
        MFRTextField = new javax.swing.JTextField();
        CCTextField = new javax.swing.JTextField();
        GPR0TextField = new javax.swing.JTextField();
        GPR1TextField = new javax.swing.JTextField();
        GPR2TextField = new javax.swing.JTextField();
        GPR3TextField = new javax.swing.JTextField();
        IXR1TextField = new javax.swing.JTextField();
        IXR2TextField = new javax.swing.JTextField();
        IXR3TextField = new javax.swing.JTextField();
        Mem0ValueTextField = new javax.swing.JTextField();
        MemoryCacheValueTextField = new javax.swing.JTextArea();
        KeyBoardArea = new javax.swing.JTextArea();
        PrinterArea = new javax.swing.JTextArea();
        MemoryCacheTextField = new javax.swing.JTextField();
        JLabel KeyboardTextLabel = new JLabel("(Enter 21 Numbers OR Words)");
        KeyboardTextLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel PrinterTextLabel = new JLabel();
        PrinterTextLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));
        Mem0RowTextField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Setting background color
        column2.setBackground(new java.awt.Color(173, 216, 230));

        // Setting Register names
        JLabel labelGpr0 = new JLabel("GPR 0");
        labelGpr0.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelGpr1 = new JLabel("GPR 1");
        labelGpr1.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelGpr2 = new JLabel("GPR 2");
        labelGpr2.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelGpr3 = new JLabel("GPR 3");
        labelGpr3.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelIxr1 = new JLabel("IXR 1");
        labelIxr1.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelIxr2 = new JLabel("IXR 2");
        labelIxr2.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelIxr3 = new JLabel("IXR 3");
        labelIxr3.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        GPR0TextField.setText("");
        GPR1TextField.setText("");
        GPR2TextField.setText("");
        GPR3TextField.setText("");
        IXR1TextField.setText("");
        IXR2TextField.setText("");
        IXR3TextField.setText("");

        JLabel labelPc = new JLabel("PC");
        labelPc.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelMar = new JLabel("MAR");
        labelMar.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelMbr = new JLabel("MBR");
        labelMbr.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelIR = new JLabel("IR");
        labelIR.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelMFR = new JLabel("MFR");
        labelMFR.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        PCTextfField.setText("");
        MARTextField.setText("");
        MBRTextField.setText("");
        IRTextField.setText("");
        MFRTextField.setText("");

        // Initializing the IPL(Init) button
        JButton initButton = new JButton("IPL");
        initButton.setBackground(new java.awt.Color(135, 206, 235));
        initButton.setActionCommand("Store");
        initButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    initButtonClick(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Initializing Porgram 1 and Program 2 buttons
        JButton program1Button = new JButton("Program 1");
        program1Button.setBackground(new java.awt.Color(135, 206, 235));
        program1Button.setActionCommand("Store");
        program1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    program1ButtonClick(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JButton program2Button = new JButton("Program 2");
        program2Button.setBackground(new java.awt.Color(135, 206, 235));
        program2Button.setActionCommand("Store");
        program2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    program2ButtonClick(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        JButton uploadValButton = new JButton("Upload Value");
        uploadValButton.setBackground(new java.awt.Color(135, 206, 235));
        uploadValButton.setActionCommand("Store");
        uploadValButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    uploadValButtonClick(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // Initializing the ST+ button
        JButton StorePlusButton = new JButton("ST+");
        StorePlusButton.setBackground(new java.awt.Color(135, 206, 235));
        StorePlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StorePlusButtonClick(evt);
            }
        });

        // Initializing the Store button
        JButton STLoadButton = new JButton("Store");
        STLoadButton.setBackground(new java.awt.Color(135, 206, 235));
        STLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadST(e);
            }
        });

        // Initializing the Instruction buttons
        JToggleButton button_15 = new JToggleButton("15");
        button_15.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_15.setBackground(new java.awt.Color(135, 206, 235));
        button_15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FifteenButtonActionPerformed(evt);
            }
        });

        JToggleButton button_14 = new JToggleButton("14");
        button_14.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_14.setBackground(new java.awt.Color(135, 206, 235));
        button_14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FourteenButtonActionPerformed(evt);
            }
        });
        JToggleButton button_13 = new JToggleButton("13");
        button_13.setBackground(new java.awt.Color(135, 206, 235));
        button_13.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_13ActionPerformed(evt);
            }
        });
        JToggleButton button_12 = new JToggleButton("12");
        button_12.setBackground(new java.awt.Color(135, 206, 235));
        button_12.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_12ActionPerformed(evt);
            }
        });

        JToggleButton button_11 = new JToggleButton("11");
        button_11.setBackground(new java.awt.Color(135, 206, 235));
        button_11.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_11ActionPerformed(evt);
            }
        });

        JToggleButton button_10 = new JToggleButton("10");
        button_10.setBackground(new java.awt.Color(135, 206, 235));
        button_10.setFont(new java.awt.Font("Times New Roman", 0, 12)); //
        button_10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_10ActionPerformed(evt);
            }
        });
        JToggleButton button_9 = new JToggleButton("9");
        button_9.setBackground(new java.awt.Color(135, 206, 235));
        button_9.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_9ActionPerformed(evt);
            }
        });
        JToggleButton button_8 = new JToggleButton("8");
        button_8.setBackground(new java.awt.Color(135, 206, 235));
        button_8.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_8ActionPerformed(evt);
            }
        });

        JToggleButton button_7 = new JToggleButton("7");
        button_7.setBackground(new java.awt.Color(135, 206, 235));
        button_7.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_7ActionPerformed(evt);
            }
        });

        JToggleButton button_6 = new JToggleButton("6");
        button_6.setBackground(new java.awt.Color(135, 206, 235));
        button_6.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_6ActionPerformed(evt);
            }
        });

        JToggleButton button_5 = new JToggleButton("5");
        button_5.setBackground(new java.awt.Color(135, 206, 235));
        button_5.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_5ActionPerformed(evt);
            }
        });
        JToggleButton button_4 = new JToggleButton("4");
        button_4.setBackground(new java.awt.Color(135, 206, 235));
        button_4.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_4ActionPerformed(evt);
            }
        });
        JToggleButton button_3 = new JToggleButton("3");
        button_3.setBackground(new java.awt.Color(135, 206, 235));
        button_3.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_3ActionPerformed(evt);
            }
        });
        JToggleButton button_2 = new JToggleButton("2");
        button_2.setBackground(new java.awt.Color(135, 206, 235));
        button_2.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_2ActionPerformed(evt);
            }
        });
        JToggleButton button_1 = new JToggleButton("1");
        button_1.setBackground(new java.awt.Color(135, 206, 235));
        button_1.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_1ActionPerformed(evt);
            }
        });
        JToggleButton button_0 = new JToggleButton("0");
        button_0.setBackground(new java.awt.Color(135, 206, 235));
        button_0.setFont(new java.awt.Font("Times New Roman", 0, 12));
        button_0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_0ActionPerformed(evt);
            }
        });

        // Instruction Label
        JLabel labelOpcode = new JLabel("Op-codes");
        labelOpcode.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelGpr = new JLabel("GPR");
        labelGpr.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel ixrLabel = new JLabel("IXR");
        ixrLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelI = new JLabel("I");
        labelI.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel labelAddress = new JLabel("Address");
        labelAddress.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        // Initializing the SS button
        JButton SingleStepButton = new JButton("SS");
        SingleStepButton.setBackground(new java.awt.Color(135, 206, 235));
        SingleStepButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SingleStepButtonActionPerformed(evt);
            }
        });

        // Initializing the Halt button
        JButton HaltButton = new JButton("HLT");
        HaltButton.setBackground(new java.awt.Color(135, 206, 235));
        HaltButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HaltButtonClick(evt);
            }
        });
        // Initializing the Load buttons for each component with correct variable and
        // method names

        JButton loadPCButton = new JButton("Load PC");
        loadPCButton.setBackground(new java.awt.Color(135, 206, 235));
        loadPCButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPC(evt); // Updated to call refactored method
            }
        });

        JButton loadMARButton = new JButton("Load MAR");
        loadMARButton.setBackground(new java.awt.Color(135, 206, 235));
        loadMARButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMAR(evt); // Updated to call refactored method
            }
        });

        JButton loadMBRButton = new JButton("Load MBR");
        loadMBRButton.setBackground(new java.awt.Color(135, 206, 235));
        loadMBRButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadMBR(evt); // Updated to call refactored method
            }
        });

        JButton loadGPR0Button = new JButton("Load GPR0");
        loadGPR0Button.setBackground(new java.awt.Color(135, 206, 235));
        loadGPR0Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGPR0(evt); // Updated to call refactored method
            }
        });

        JButton loadGPR1Button = new JButton("Load GPR1");
        loadGPR1Button.setBackground(new java.awt.Color(135, 206, 235));
        loadGPR1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGPR1(evt); // Updated to call refactored method
            }
        });

        JButton loadGPR2Button = new JButton("Load GPR2");
        loadGPR2Button.setBackground(new java.awt.Color(135, 206, 235));
        loadGPR2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGPR2(evt); // Updated to call refactored method
            }
        });

        JButton loadGPR3Button = new JButton("Load GPR3");
        loadGPR3Button.setBackground(new java.awt.Color(135, 206, 235));
        loadGPR3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGPR3(evt); // Updated to call refactored method
            }
        });

        JButton loadIXR1Button = new JButton("Load IXR1");
        loadIXR1Button.setBackground(new java.awt.Color(135, 206, 235));
        loadIXR1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadIXR1(evt); // Updated to call refactored method
            }
        });

        JButton loadIXR2Button = new JButton("Load IXR2");
        loadIXR2Button.setBackground(new java.awt.Color(135, 206, 235));
        loadIXR2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadIXR2(evt); // Updated to call refactored method
            }
        });

        JButton loadIXR3Button = new JButton("Load IXR3");
        loadIXR3Button.setBackground(new java.awt.Color(135, 206, 235));
        loadIXR3Button.setPreferredSize(new Dimension(1500, 5000));
        loadIXR3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadIXR3(evt); // Updated to call refactored method
            }
        });

        CCTextField.setText("jTextField12");

        JLabel labelCC = new JLabel("CC");
        labelCC.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        // Initializing the ultimate Load button
        JButton UltimateLoadButton = new JButton("Load");
        UltimateLoadButton.setBackground(new java.awt.Color(135, 206, 235));
        UltimateLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                UltimateLoadAction(e);
            }
        });

        // Initializing the Reset button
        JButton ResetButton = new JButton("Reset");
        ResetButton.setBackground(new java.awt.Color(135, 206, 235));
        ResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetButtonClick(evt);
            }
        });

        // Initializing the text fields for each component
        Mem0ValueTextField.setText("jTextField3");
        MemoryCacheValueTextField.setEditable(false); // Make it read-only
        MemoryCacheValueTextField.setLineWrap(true); // Wrap long lines
        MemoryCacheValueTextField.setWrapStyleWord(true); // Wrap at word boundaries

        JScrollPane scrollPane = new JScrollPane(MemoryCacheValueTextField);
        scrollPane.setPreferredSize(new Dimension(200, 50));
        KeyBoardArea.setEditable(true); // Make it read-only
        KeyBoardArea.setLineWrap(true); // Wrap long lines
        KeyBoardArea.setWrapStyleWord(true); // Wrap at word boundaries

        JScrollPane scrollPaneKeyBoard = new JScrollPane(KeyBoardArea);
        // Limit the height of the JScrollPane (width: 300, height: 100)
        scrollPaneKeyBoard.setPreferredSize(new Dimension(200, 100));
        scrollPaneKeyBoard.setMinimumSize(new Dimension(200, 100));
        scrollPaneKeyBoard.setMaximumSize(new Dimension(200, 100));

        JScrollPane scrollPaneKeyBoardPrinter = new JScrollPane(PrinterArea);
        PrinterArea.setEditable(false);
        // Limit the height of the JScrollPane (width: 300, height: 100)
        scrollPaneKeyBoardPrinter.setPreferredSize(new Dimension(200, 100));
        scrollPaneKeyBoardPrinter.setMinimumSize(new Dimension(200, 100));
        scrollPaneKeyBoardPrinter.setMaximumSize(new Dimension(200, 100));

        // Count of numbers entered
        final int MAX_NUMBERS = 20;
        JFrame frame = new JFrame("KeyBoard Input App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLayout(new BorderLayout());

        KeyBoardArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!Processor.fileOpened) {
                    JOptionPane.showMessageDialog(frame, "Please select demo.txt file first.", "File Required",
                            JOptionPane.WARNING_MESSAGE);
                    KeyBoardArea.setText(""); // Clear the text area
                    return; // Exit the method if no file is opened
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String inputText = KeyBoardArea.getText().trim();
                    String[] numbers = inputText.split("\\n");

                    if (numbers.length > numberCount) {
                        try {
                            int currentNumber = Integer.parseInt(numbers[numberCount]);
                            if (numberCount < MAX_NUMBERS) {
                                // Store the first 20 numbers
                                enteredNumbers[numberCount] = currentNumber;
                            }
                            numberCount++;

                            // Display the current number in PrinterArea
                            PrinterArea.setText(String.valueOf(currentNumber));

                            // If 20 numbers have been entered, prompt for the 21st
                            if (numberCount == MAX_NUMBERS) {
                                String nextNumberStr = JOptionPane.showInputDialog(
                                        frame,
                                        "Enter the 21st number:",
                                        "Input Required",
                                        JOptionPane.QUESTION_MESSAGE);

                                if (nextNumberStr != null && !nextNumberStr.isEmpty()) {
                                    int nextNumber = Integer.parseInt(nextNumberStr);
                                    KeyBoardArea.append("\n" + nextNumber);

                                    // Find the closest number from the first 20 numbers
                                    int closestNumber = findClosestNumber(nextNumber, enteredNumbers);
                                    doMaths();
                                    SwingUtilities.invokeLater(() -> {
                                        PrinterArea.setText(String.valueOf(closestNumber));
                                    });
                                    Processor.setRegisterVals("GPR1", strFormatter.intToBinaryArray(closestNumber));
                                    Processor.setRegisterVals("GPR2", new int[16]);
                                    Processor.setRegisterVals("GPR3", new int[16]);
                                    Processor.setRegisterVals("MBR", strFormatter.intToBinaryArray(closestNumber));
                                }

                                numberCount = 0; // Reset count after reaching max
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Please enter a valid number.", "Invalid Input",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        JLabel MemoryCachelabelConsole = new JLabel("MemoryCache");
        MemoryCachelabelConsole.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel KeyBoardLabel = new JLabel("Keyboard");
        KeyBoardLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        JLabel PrinterLabel = new JLabel("Printer");
        PrinterLabel.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 17));

        Mem0RowTextField.setText("jTextField1");
        MemoryCacheTextField.setText("Cache Missed ?");
        MemoryCacheTextField.setEditable(false);

        JLabel marValueLabel = new JLabel("");
        marValueLabel.setFont(new java.awt.Font("Times New Roman", 1, 13));

        // Initializing the Run button
        JButton RunButton = new JButton("Run");
        RunButton.setBackground(new java.awt.Color(135, 206, 235));
        RunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunButtonClick(evt);
            }
        });
        column2.setPreferredSize(new Dimension(1800, 60000));

        JLabel subjectLabel = new JLabel("  CSCI_6461_12 - Computer System Architecture  ");
        subjectLabel.setFont(new Font("Monospaced", java.awt.Font.BOLD, 42));
        // JLabel subheadingLabel = new JLabel("Computer GUI");
        // subheadingLabel.setFont(new Font("Times New Roman", Font.BOLD, 32));

        // UI of column 1
        javax.swing.GroupLayout column1Layout = new javax.swing.GroupLayout(column1);
        column1.setLayout(column1Layout);
        column1Layout.setHorizontalGroup(
                column1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(column1Layout.createSequentialGroup()
                                .addGap(0)
                                .addGroup(column1Layout.createSequentialGroup()
                                        .addComponent(column2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(32, 42, Short.MAX_VALUE))));
        column1Layout.setVerticalGroup(
                column1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(column1Layout.createSequentialGroup()
                                .addComponent(column2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, Short.MAX_VALUE)));
        // UI of column 2
        // Panel Layout for every component
        javax.swing.GroupLayout column2Layout = new javax.swing.GroupLayout(column2);
        column2.setLayout(column2Layout);
        // horizontal layout for column 2
        column2Layout.setHorizontalGroup(
                column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(column2Layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addGap(5, 5, 5)// centers the main text horixontally!
                                .addComponent(subjectLabel))
                        .addGroup(column2Layout.createSequentialGroup()
                                .addGroup(column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGap(10, 10, 10) // adds margin to horizontal line(opcode waali line)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(labelOpcode,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 194,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addComponent(button_15,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(button_14,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(button_13,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(button_12,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(button_11,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(button_10,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 40,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))))
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGap(52, 52, 52)
                                                .addComponent(labelIR)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(IRTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 256,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGap(52, 52, 52)
                                                .addComponent(labelPc)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addComponent(PCTextfField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 256,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(loadPCButton))))
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGap(36, 36, 36)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addComponent(labelCC)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(CCTextField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 257,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(labelMar,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addComponent(labelMbr,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING))
                                                                                .addGap(5, 5, 5))
                                                                        .addComponent(labelMFR))
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGap(1, 1, 1)
                                                                                .addComponent(MARTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        256,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                .addComponent(MFRTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        257,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(MBRTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        256,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(loadMARButton)
                                                        .addComponent(loadMBRButton))))
                                .addGap(87, 87, Short.MAX_VALUE)
                                .addGroup(column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                column2Layout.createSequentialGroup()
                                                        .addGap(0, 280, Short.MAX_VALUE)
                                                        .addComponent(initButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(program1Button)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(program2Button)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(StorePlusButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(STLoadButton)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(UltimateLoadButton)
                                                        .addGap(852, 852, 852))
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addComponent(labelIxr1)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(IXR1TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        244,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(loadIXR1Button))
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(column2Layout
                                                                                                .createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                .addGroup(column2Layout
                                                                                                        .createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                false)
                                                                                                        .addGroup(
                                                                                                                column2Layout
                                                                                                                        .createSequentialGroup()
                                                                                                                        .addComponent(
                                                                                                                                labelGpr3)
                                                                                                                        .addPreferredGap(
                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                        .addComponent(
                                                                                                                                GPR3TextField))
                                                                                                        .addGroup(
                                                                                                                column2Layout
                                                                                                                        .createSequentialGroup()
                                                                                                                        .addComponent(
                                                                                                                                labelGpr2)
                                                                                                                        .addPreferredGap(
                                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                                        .addComponent(
                                                                                                                                GPR2TextField,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                242,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                                .addGroup(column2Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(
                                                                                                                labelGpr0)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                        .addGroup(
                                                                                                                column2Layout
                                                                                                                        .createParallelGroup(
                                                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                                        .addComponent(
                                                                                                                                GPR0TextField,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                                241,
                                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                                                        .addGroup(column2Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(labelGpr1)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                                .addComponent(
                                                                                                        GPR1TextField,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        242,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(loadGPR0Button)
                                                                                        .addComponent(loadGPR1Button)
                                                                                        .addComponent(loadGPR2Button)
                                                                                        .addComponent(loadGPR3Button)))
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(labelIxr2,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                        .addComponent(labelIxr3,
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING))
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                false)
                                                                                        .addComponent(IXR2TextField)
                                                                                        .addComponent(IXR3TextField,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                244,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(loadIXR2Button)
                                                                                        .addComponent(
                                                                                                loadIXR3Button)))))
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING,
                                                                        false)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addComponent(button_9,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        40,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(button_8,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        40,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addComponent(labelGpr,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                66,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addComponent(button_7,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        40,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(button_6,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        40,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addComponent(ixrLabel,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                61,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(button_5,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                40,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(labelI,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                22,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addGap(206, 206, 206)
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                false)
                                                                                        .addGroup(column2Layout
                                                                                                .createSequentialGroup()
                                                                                                .addComponent(button_4,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        40,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(button_3,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        40,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(button_2,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        40,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(button_1,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        40,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                                .addPreferredGap(
                                                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                                                .addComponent(button_0,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        40,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addComponent(labelAddress,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                174,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                .addGap(33, 33, 33)
                                                                                .addComponent(SingleStepButton,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        64,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(
                                                                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(HaltButton,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                64,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(ResetButton,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                64,
                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(RunButton)))
                                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                false)
                                                                                        .addGroup(column2Layout
                                                                                                .createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                                                .addGap(223)
                                                                                                .addGroup(column2Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                MemoryCachelabelConsole)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                MemoryCacheTextField,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                .addGroup(column2Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                KeyBoardLabel)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                KeyboardTextLabel,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                                .addGroup(column2Layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                PrinterLabel)
                                                                                                        .addPreferredGap(
                                                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                Short.MAX_VALUE)
                                                                                                        .addComponent(
                                                                                                                PrinterTextLabel,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                javax.swing.GroupLayout.PREFERRED_SIZE))

                                                                                        ))
                                                                                .addGap(16, 16, 16)
                                                                                .addGroup(column2Layout
                                                                                        .createParallelGroup(
                                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                        .addGroup(column2Layout
                                                                                                .createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                        false)
                                                                                                .addComponent(
                                                                                                        scrollPane,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                                        425,
                                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                                        .addGroup(column2Layout
                                                                                                .createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                        false)
                                                                                                .addComponent(
                                                                                                        scrollPaneKeyBoard))
                                                                                        .addGroup(column2Layout
                                                                                                .createParallelGroup(
                                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                                        false)
                                                                                                .addComponent(
                                                                                                        scrollPaneKeyBoardPrinter))
                                                                                        .addComponent(
                                                                                                marValueLabel))))))))));
        // vertical layout for column 2
        column2Layout.setVerticalGroup(
                column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(column2Layout.createSequentialGroup()
                                .addComponent(subjectLabel)
                                .addGap(16, 16, 16))
                        .addGroup(column2Layout.createSequentialGroup()
                                .addGap(10, 10, 10)// adds margin to panels vertically
                                .addGroup(column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(marValueLabel,
                                                                javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addGap(220)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(scrollPane,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 155,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(MemoryCachelabelConsole)
                                                        .addComponent(MemoryCacheTextField,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(scrollPaneKeyBoard)
                                                        .addComponent(KeyBoardLabel)
                                                        .addComponent(KeyboardTextLabel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(scrollPaneKeyBoardPrinter)
                                                        .addComponent(PrinterLabel)
                                                        .addComponent(PrinterTextLabel,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGap(8, 8, 8)// MARGIN TOP TO 2 boxes of registers
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(initButton)
                                                        .addComponent(program1Button)
                                                        .addComponent(program2Button)
                                                        .addComponent(StorePlusButton)
                                                        .addComponent(STLoadButton)
                                                        .addComponent(UltimateLoadButton)))
                                        .addGroup(column2Layout
                                                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, column2Layout
                                                        .createSequentialGroup()
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.TRAILING)
                                                                .addComponent(labelGpr0,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 17,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGroup(column2Layout.createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                        .addComponent(GPR0TextField,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                25,
                                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(loadGPR0Button)))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(labelGpr1)
                                                                .addComponent(GPR1TextField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(loadGPR1Button))
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addGroup(column2Layout.createSequentialGroup()
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(labelGpr2)
                                                                                .addComponent(GPR2TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(loadGPR2Button))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(labelGpr3)
                                                                                .addComponent(GPR3TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(loadGPR3Button))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(labelIxr1)
                                                                                .addComponent(IXR1TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(loadIXR1Button))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(labelIxr2)
                                                                                .addComponent(IXR2TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(loadIXR2Button))
                                                                        .addPreferredGap(
                                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(IXR3TextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                                .addComponent(labelIxr3)
                                                                                .addComponent(loadIXR3Button)))
                                                                .addGroup(column2Layout.createSequentialGroup()
                                                                        .addGap(103, 103, 103)
                                                                        .addGroup(column2Layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(labelCC)
                                                                                .addComponent(CCTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        25,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))))
                                                        .addGap(97, 97, 97))
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(IRTextField,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(labelIR))
                                                .addGroup(column2Layout.createSequentialGroup()
                                                        .addGap(30, 30, 30)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(labelPc,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 17,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(loadPCButton)
                                                                .addComponent(PCTextfField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(MARTextField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(labelMar)
                                                                .addComponent(loadMARButton))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(MBRTextField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(labelMbr)
                                                                .addComponent(loadMBRButton))
                                                        .addPreferredGap(
                                                                javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(column2Layout
                                                                .createParallelGroup(
                                                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(MFRTextField,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE, 25,
                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(labelMFR))
                                                        .addGap(153, 153, 153))))
                                .addGroup(column2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(column2Layout.createSequentialGroup()
                                                .addGap(57, 57, 57)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(button_15, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_13, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_14, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_12, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_10, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_11, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_9, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_8, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_7, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_6, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_5, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_4, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_2, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_3, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(button_0, javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                40, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(labelOpcode)
                                                        .addComponent(labelGpr)
                                                        .addComponent(ixrLabel)
                                                        .addComponent(labelI)
                                                        .addComponent(labelAddress)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, column2Layout
                                                .createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addGroup(column2Layout
                                                        .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(SingleStepButton,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE, 92,
                                                                javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(column2Layout.createSequentialGroup()
                                                                .addComponent(ResetButton)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(RunButton)
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(HaltButton)))))
                                .addGap(45, 45, 45)));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(column1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(column1, javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 8, Short.MAX_VALUE)));

        pack();
    }

    // on click actions of the switches
    private void FifteenButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[0] == 0) {
            instructionArray[0] = 1;
        } else {
            instructionArray[0] = 0;
        }
    }

    private void button_13ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[2] == 0) {
            instructionArray[2] = 1;
        } else {
            instructionArray[2] = 0;
        }
    }

    private void button_12ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[3] == 0) {
            instructionArray[3] = 1;
        } else {
            instructionArray[3] = 0;
        }
    }

    private void button_10ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[5] == 0) {
            instructionArray[5] = 1;
        } else {
            instructionArray[5] = 0;
        }
    }

    private void button_9ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[6] == 0) {
            instructionArray[6] = 1;
        } else {
            instructionArray[6] = 0;
        }
    }

    private void button_5ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[10] == 0) {
            instructionArray[10] = 1;
        } else {
            instructionArray[10] = 0;
        }
    }

    private void button_7ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[8] == 0) {
            instructionArray[8] = 1;
        } else {
            instructionArray[8] = 0;
        }
    }

    private void button_1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[14] == 0) {
            instructionArray[14] = 1;
        } else {
            instructionArray[14] = 0;
        }
    }

    private void button_2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[13] == 0) {
            instructionArray[13] = 1;
        } else {
            instructionArray[13] = 0;
        }
    }

    private void button_4ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[11] == 0) {
            instructionArray[11] = 1;
        } else {
            instructionArray[11] = 0;
        }
    }

    private void SingleStepButtonActionPerformed(java.awt.event.ActionEvent evt) {
        ///do stuff here
        String instructionAddress = MemoryCache.fetchVal(Arrays.toString(Processor.getRegisterVals("PC")));
        System.out.println(instructionAddress);

        int[] instruction_address = Processor.getRegisterVals("PC");
        int int_instruction_address = strFormatter.binToInt(instruction_address);
        Processor.setRegisterVals("IR", Processor.getMemoryVals(int_instruction_address));

        if (instructionAddress == null) {
            MemoryCacheTextField.setText("Cache miss!");
            System.out.println("Cache Missed !!");
            MemoryCache.insertVal(Arrays.toString(Processor.getRegisterVals("PC")),
                    Arrays.toString(Processor.getRegisterVals("IR")));
            StringBuilder textContent = new StringBuilder();
            for (String key : MemoryCache.cache.keySet()) {
                String value = MemoryCache.cache.get(key);
                textContent.append("MAR: ").append(key).append(", MBR: ").append(value).append("; ");
            }

            // Set the concatenated string as the text in the JTextField
            MemoryCacheValueTextField.setText(textContent.toString());

            System.out
                    .println(Arrays.toString(Processor.getRegisterVals("PC"))
                            + Arrays.toString(Processor.getRegisterVals("IR")));
        } else {
            MemoryCacheTextField.setText("MemoryCache Hit!");
            System.out.println("MemoryCache Hit!");
        }
        Processor.execute("single");
    }

    private void button_0ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[15] == 0) {
            instructionArray[15] = 1;
        } else {
            instructionArray[15] = 0;
        }
    }

    private void button_3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[12] == 0) {
            instructionArray[12] = 1;
        } else {
            instructionArray[12] = 0;
        }
    }

    private void button_6ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[9] == 0) {
            instructionArray[9] = 1;
        } else {
            instructionArray[9] = 0;
        }
    }

    private void button_8ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[7] == 0) {
            instructionArray[7] = 1;
        } else {
            instructionArray[7] = 0;
        }
    }

    private void button_11ActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[4] == 0) {
            instructionArray[4] = 1;
        } else {
            instructionArray[4] = 0;
        }
    }

    private void FourteenButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (instructionArray[1] == 0) {
            instructionArray[1] = 1;
        } else {
            instructionArray[1] = 0;
        }
    }

    // Helper method to load values into Processor registers
    private void loadRegister(String registerName, int startIndex, int endIndex) {
        Processor.setRegisterVals(registerName, Arrays.copyOfRange(instructionArray, startIndex, endIndex));
    }

    // PC Load Button action
    private void loadPC(java.awt.event.ActionEvent evt) {
        loadRegister("PC", 4, 16);
    }

    // MAR Load Button action
    private void loadMAR(java.awt.event.ActionEvent evt) {
        loadRegister("MAR", 4, 16);
    }

    // IR Load Button action
    private void loadIR(java.awt.event.ActionEvent evt) {
        loadRegister("IR", 0, 16);
    }

    // MBR Load Button action
    private void loadMBR(java.awt.event.ActionEvent evt) {
        loadRegister("MBR", 0, 16);
    }

    // GPR0 Load Button action
    private void loadGPR0(java.awt.event.ActionEvent evt) {
        loadRegister("GPR0", 0, 16);
    }

    // GPR1 Load Button action
    private void loadGPR1(java.awt.event.ActionEvent evt) {
        loadRegister("GPR1", 0, 16);
    }

    // GPR2 Load Button action
    private void loadGPR2(java.awt.event.ActionEvent evt) {
        loadRegister("GPR2", 0, 16);
    }

    // GPR3 Load Button action
    private void loadGPR3(java.awt.event.ActionEvent evt) {
        loadRegister("GPR3", 0, 16);
    }

    // IXR1 Load Button action
    private void loadIXR1(java.awt.event.ActionEvent evt) {
        loadRegister("IX1", 0, 16);
    }

    // IXR2 Load Button action
    private void loadIXR2(java.awt.event.ActionEvent evt) {
        loadRegister("IX2", 0, 16);
    }

    // IXR3 Load Button action
    private void loadIXR3(java.awt.event.ActionEvent evt) {
        loadRegister("IX3", 0, 16);
    }

    // Store (ST) Load Button action
    private void loadST(java.awt.event.ActionEvent evt) {
        int[] cur_MAR = Processor.getRegisterVals("MAR");
        int trans_MAR = strFormatter.binToInt(cur_MAR);
        Processor.setMemoryVals(trans_MAR, Processor.getRegisterVals("MBR"));
    }

    private void initButtonClick(java.awt.event.ActionEvent evt) throws IOException {

        try {
            Processor.openFileChooser();
        } catch (IOException ex) {
            System.out.println("Something went wrong");
        }
    }

    private void uploadValButtonClick(java.awt.event.ActionEvent evt) throws IOException {
        try {
            Processor.openFileChooser();
        } catch (IOException ex) {
            System.out.println("Something went wrong");
        }
    }

    private void program1ButtonClick(java.awt.event.ActionEvent evt) throws IOException {
        try {
            Processor.openFileChooser();
        } catch (IOException ex) {
            System.out.println("Something went wrong");
        }
    }

    JFrame frame2 = new JFrame("My Application");

    private void program2ButtonClick(java.awt.event.ActionEvent evt) throws IOException {
        try {
            Processor.openFileChooser();
        } catch (IOException ex) {
            System.out.println("Something went wrong");
        }
        if (!Processor.fileOpened2) {
            JOptionPane.showMessageDialog(frame2, "Please select program 2 file first.", "File Required",
                    JOptionPane.WARNING_MESSAGE);
            KeyBoardArea.setText(""); // Clear the text area
            return; // Exit the method if no file is opened
        }
        try {
            // Step 1: Read the paragraph from a file
            File file = new File("Demo.txt");
            Scanner fileScanner = new Scanner(file);
            StringBuilder paragraphBuilder = new StringBuilder();
            while (fileScanner.hasNextLine()) {
                paragraphBuilder.append(fileScanner.nextLine()).append(" ");
            }
            fileScanner.close();

            // Split the paragraph into sentences
            String paragraph = paragraphBuilder.toString().trim();
            String[] sentences = paragraph.split("(?<=\\.)"); // Splitting sentences by '.'

            // Print sentences
            System.out.println("Loaded Content in Demo File is :\n");
            for (int i = 0; i < sentences.length; i++) {
                System.out.println((i + 1) + ": " + sentences[i].trim());
            }
            StringBuilder para = new StringBuilder();
            for (int i = 0; i < sentences.length; i++) {
                para.append((i + 1) + ": " + sentences[i].trim() + "\n");
            }
            PrinterArea.setText(para.toString());
            JFrame frame1 = new JFrame("KeyBoard Input App");
            frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame1.setSize(500, 300);
            frame1.setLayout(new BorderLayout());
            String wordStr = JOptionPane.showInputDialog(
                    frame1,
                    "Enter the word to search:",
                    "Input Required",
                    JOptionPane.QUESTION_MESSAGE);
            // Step 3: Search for the word in sentences
            boolean found = false;
            StringBuilder wordFound = new StringBuilder();
            for (int i = 0; i < sentences.length; i++) {
                String[] words = sentences[i].trim().split("\\s+"); // Split sentence into words
                for (int j = 0; j < words.length; j++) {
                    // Remove punctuation for matching
                    String cleanedWord = words[j].replaceAll("[^a-zA-Z0-9]", "");
                    if (cleanedWord.equalsIgnoreCase(wordStr)) {
                        System.out.println("The Searched Word is: \"" + wordStr + "\"" + "\n");
                        wordFound.append("\n" + "The Searched Word is:" + wordStr + "\n");
                        System.out.println("Sentence Line is: " + (i + 1));
                        wordFound.append("Sentence Line is: " + (i + 1) + "\n");
                        System.out.println("Location in sentence " + (i + 1) + " : " + (j + 1));
                        wordFound.append("location in sentence " + (i + 1) + ": " + (j + 1) + "\n");
                        found = true;
                    }
                }
            }
            if (!found) {
                System.out.println("The word \"" + wordStr + "\" was not found in the paragraph.");
                wordFound.append("The word \"" + wordStr + "\" was not found in the paragraph." + "\n");
            }
            PrinterArea.setText(wordFound.toString());

        } catch (FileNotFoundException e) {
            System.err.println("File not found. Please ensure 'Demo.txt' exists in the same directory.");
        }
    }

    // Ultimate load functionality(takes address from MAR and load memory content to
    // MBR
    // MBR = memory[MAR])
    private void UltimateLoadAction(java.awt.event.ActionEvent evt) {
        // Set MBR equal to the value located at MAR in memory
        int[] cur_MAR = Processor.getRegisterVals("MAR");
        int trans_MAR = strFormatter.binToInt(cur_MAR);
        Processor.setRegisterVals("MBR", Processor.getMemoryVals(trans_MAR));
    }

    //
    private void StorePlusButtonClick(java.awt.event.ActionEvent evt) {
        // Set Memory[MAR] to MBR
        int[] cur_MAR = Processor.getRegisterVals("MAR");
        int trans_MAR = strFormatter.binToInt(cur_MAR);
        Processor.setMemoryVals(trans_MAR, Processor.getRegisterVals("MBR"));
        // Increment MAR by 1
        trans_MAR = trans_MAR + 1;
        int[] new_MAR = Processor.intToBinaryArrayShort(Integer.toBinaryString(trans_MAR));
        Processor.setRegisterVals("MAR", new_MAR);
    }

    private void ResetButtonClick(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_ResetButtonActionPerformed
        // Reset registers
        int[] Reset_large = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] Reset_medium = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        int[] Reset_small = new int[] { 0, 0, 0, 0 };

        Processor.setRegisterVals("IR", Reset_large);
        Processor.setRegisterVals("MBR", Reset_large);
        Processor.setRegisterVals("IX1", Reset_large);
        Processor.setRegisterVals("IX2", Reset_large);
        Processor.setRegisterVals("IX3", Reset_large);
        Processor.setRegisterVals("GPR3", Reset_large);
        Processor.setRegisterVals("GPR0", Reset_large);
        Processor.setRegisterVals("GPR1", Reset_large);
        Processor.setRegisterVals("GPR2", Reset_large);

        Processor.setRegisterVals("PC", Reset_medium);
        Processor.setRegisterVals("MAR", Reset_medium);

        Processor.setRegisterVals("CC", Reset_small);
        Processor.setRegisterVals("MFR", Reset_small);
        MemoryCache.cache.clear();
        MemoryCacheValueTextField.setText("");
        PrinterArea.setText("");
        KeyBoardArea.setText("");
        Processor.fileOpened = false;

    }

    private void HaltButtonClick(java.awt.event.ActionEvent evt) {
        int[] msg = new int[] { 1 };
        Processor.setRegisterVals("HLT", msg);
    }

    private void RunButtonClick(java.awt.event.ActionEvent evt) {
        if (run_check == false) {
            run_check = true;
        } else {
            run_check = false;
        }
    }

    // Function to update the values of the registers
    public void updateRegisters() {
        // Update each register field
        updateRegisterField("PC", PCTextfField);
        updateRegisterField("CC", CCTextField);
        updateRegisterField("MAR", MARTextField);
        updateMemoryDisplay(); // Still need to call this after MAR update
        updateRegisterField("MBR", MBRTextField);
        updateRegisterField("IR", IRTextField);
        updateRegisterField("MFR", MFRTextField);
        updateRegisterField("IX1", IXR1TextField);
        updateRegisterField("IX2", IXR2TextField);
        updateRegisterField("IX3", IXR3TextField);
        updateRegisterField("GPR0", GPR0TextField);
        updateRegisterField("GPR1", GPR1TextField);
        updateRegisterField("GPR2", GPR2TextField);
        updateRegisterField("GPR3", GPR3TextField);
    }

    private void updateRegisterField(String registerName, JTextField textField) {
        int[] tmp_array = Processor.getRegisterVals(registerName);
        textField.setText(formatText(tmp_array));
    }

    // Function to update the display with values stored in memory at MAR location
    public void updateMemoryDisplay() {
        int[] cur_MAR = Processor.getRegisterVals("MAR");
        int trans_MAR = strFormatter.binToInt(cur_MAR);

        // Update display for current MAR location
        updateMemoryField(trans_MAR, Mem0ValueTextField, Mem0RowTextField);

        // Update display for MAR+1 and MAR+2
        updateMemoryField(trans_MAR + 1, null, null); // Ignoring text fields for MAR+1
        updateMemoryField(trans_MAR + 2, null, null); // Ignoring text fields for MAR+2

        // If MAR > 2, update for MAR-1 and MAR-2
        if (trans_MAR > 2) {
            updateMemoryField(trans_MAR - 1, null, null); // Ignoring text fields for MAR-1
            updateMemoryField(trans_MAR - 2, null, null); // Ignoring text fields for MAR-2
        }
    }

    private void updateMemoryField(int address, JTextField valueField, JTextField rowField) {
        int[] memoryValue = Processor.getMemoryVals(address);

        if (valueField != null) {
            valueField.setText(formatText(memoryValue));
        }
        if (rowField != null) {
            rowField.setText(Integer.toString(address));
        }
    }

    // Function to update the display with values stored in memory at MAR location
    // Function to format int[] values to display on the GUI
    public String formatText(int[] arr) {
        String res = Arrays.toString(arr).replaceAll("[\\[\\],]", "");
        return res;
    }

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            System.out.println(GUI.class.getName() + ex);
        } catch (InstantiationException ex) {
            System.out.println(GUI.class.getName() + ex);
        } catch (IllegalAccessException ex) {
            System.out.println(GUI.class.getName() + ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.out.println(GUI.class.getName() + ex);
        }
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}