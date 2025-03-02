import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class MachineSimulator1 {

	private static final int MEMORY_SIZE = 2048;
	private int[] memory = new int[MEMORY_SIZE];

	private int[] GPR = new int[4];  // General Purpose Registers R0-R3
	private int[] IXR = new int[3];  // Index Registers
	private int PC, MAR, MBR, IR, CC, MFR;  // Other Registers
	private JTextField[] gprFields = new JTextField[4];
	private JTextField[] ixrFields = new JTextField[3];
	private JTextField pcField, marField, mbrField, irField, ccField, mfrField, octalInputField, binaryField, consoleInputField;

	private static final int BOOT_START_ADDR = 010;  // Octal 10
	private static final int PROGRAM_START_ADDR = 020;  // Octal 10 + 10 = 020

	private JTextArea consoleOutput, cacheContent, printerArea;
	private JButton runButton, stepButton, haltButton, iplButton, loadButton, storeButton, loadPlusButton, storePlusButton;

	// Declare the frame variable
	private JFrame frame;

	public MachineSimulator1() {
		initializeMemory();
		createUI();
	}

	// Initialize memory to zeros
	private void initializeMemory() {
		Arrays.fill(memory, 0);
	}

	// Create UI and buttons
	private void createUI() {
		// Initialize the frame
		frame = new JFrame("CSCI 6461 Machine Simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);
		frame.setLayout(new BorderLayout());

		frame.getContentPane().setBackground(new Color(173, 216, 230));

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setOpaque(false);

		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		JPanel gprPanel = new JPanel(new GridLayout(5, 2, 5, 5));
		gprPanel.setOpaque(false);
		for (int i = 0; i < 4; i++) {
			gprFields[i] = new JTextField("0", 5);
			gprPanel.add(new JLabel("GPR " + i));
			gprPanel.add(gprFields[i]);
		}

		JPanel ixrPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		ixrPanel.setOpaque(false);
		for (int i = 0; i < 3; i++) {
			ixrFields[i] = new JTextField("0", 5);
			ixrPanel.add(new JLabel("IXR " + i));
			ixrPanel.add(ixrFields[i]);
		}

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		centerPanel.add(gprPanel, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		centerPanel.add(ixrPanel, gbc);

		JPanel registerPanel = new JPanel(new GridLayout(3, 4, 5, 5));
		registerPanel.setOpaque(false);
		pcField = new JTextField("0", 5);
		marField = new JTextField("0", 5);
		mbrField = new JTextField("0", 5);
		irField = new JTextField("0", 5);
		ccField = new JTextField("0", 5);
		mfrField = new JTextField("0", 5);

		registerPanel.add(new JLabel("PC"));
		registerPanel.add(pcField);
		registerPanel.add(new JLabel("MAR"));
		registerPanel.add(marField);
		registerPanel.add(new JLabel("MBR"));
		registerPanel.add(mbrField);
		registerPanel.add(new JLabel("IR"));
		registerPanel.add(irField);
		registerPanel.add(new JLabel("CC"));
		registerPanel.add(ccField);
		registerPanel.add(new JLabel("MFR"));
		registerPanel.add(mfrField);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 4;
		centerPanel.add(registerPanel, gbc);

		JPanel cachePrinterPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		cachePrinterPanel.setOpaque(false);
		cacheContent = new JTextArea(5, 20);
		cacheContent.setText("Cache Contents");
		cacheContent.setEditable(false);

		printerArea = new JTextArea(5, 20);
		printerArea.setText("Printer Output");
		printerArea.setEditable(false);

		cachePrinterPanel.add(new JLabel("Cache Content"));
		cachePrinterPanel.add(new JScrollPane(cacheContent));
		cachePrinterPanel.add(new JLabel("Printer"));
		cachePrinterPanel.add(new JScrollPane(printerArea));

		binaryField = new JTextField(16);
		binaryField.setEditable(false);
		binaryField.setBackground(Color.WHITE);

		octalInputField = new JTextField("0", 5);

		JPanel binaryOctalPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		binaryOctalPanel.setOpaque(false);
		binaryOctalPanel.add(new JLabel("BINARY"));
		binaryOctalPanel.add(binaryField);
		binaryOctalPanel.add(new JLabel("OCTAL INPUT"));
		binaryOctalPanel.add(octalInputField);

		consoleInputField = new JTextField(20);
		JPanel consoleInputPanel = new JPanel(new FlowLayout());
		consoleInputPanel.setOpaque(false);
		consoleInputPanel.add(new JLabel("Console Input"));
		consoleInputPanel.add(consoleInputField);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		centerPanel.add(binaryOctalPanel, gbc);

		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		centerPanel.add(cachePrinterPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		centerPanel.add(consoleInputPanel, gbc);

		consoleOutput = new JTextArea(10, 40);
		consoleOutput.setEditable(false);
		JScrollPane consoleScrollPane = new JScrollPane(consoleOutput);
		frame.add(consoleScrollPane, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 8, 5, 5));
		buttonPanel.setOpaque(false);

		iplButton = new JButton("IPL");
		iplButton.addActionListener(new IPLActionListener());

		loadButton = new JButton("Load");
		loadButton.addActionListener(new LoadActionListener());

		storeButton = new JButton("Store");
		storeButton.addActionListener(new StoreActionListener());

		loadPlusButton = new JButton("Load+");
		storePlusButton = new JButton("Store+");

		runButton = new JButton("Run");
		runButton.addActionListener(new RunActionListener());
		stepButton = new JButton("Step");
		stepButton.addActionListener(new StepActionListener());

		haltButton = new JButton("Halt");
		haltButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printerArea.append("Program halted manually by clicking halt button");
				stopProgram();
			}
		});

		Color buttonColor = new Color(70, 130, 180);
		Font buttonFont = new Font("Arial", Font.BOLD, 12);
		JButton[] buttons = {iplButton, loadButton, storeButton, loadPlusButton, storePlusButton, runButton, stepButton, haltButton};
		for (JButton button : buttons) {
			button.setBackground(buttonColor);
			button.setForeground(Color.WHITE);
			button.setFont(buttonFont);
		}

		buttonPanel.add(loadButton);
		buttonPanel.add(storeButton);
		buttonPanel.add(iplButton);
		buttonPanel.add(loadPlusButton);
		buttonPanel.add(runButton);
		buttonPanel.add(stepButton);
		buttonPanel.add(haltButton);
		buttonPanel.add(storePlusButton);

		JTextField programFileField = new JTextField(20);
		JPanel programFilePanel = new JPanel(new FlowLayout());
		programFilePanel.setOpaque(false);
		programFilePanel.add(new JLabel("Program File"));
		programFilePanel.add(programFileField);

		frame.add(centerPanel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.SOUTH);
		frame.add(programFilePanel, BorderLayout.NORTH);

		frame.setVisible(true);
	}

	// IPL button action listener for loading the boot program
	private class IPLActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			int result = fileChooser.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				// Get the selected file
				java.io.File romFile = fileChooser.getSelectedFile();
				try {
					loadROMFile(romFile);  // Load the ROM file into memory
					printerArea.append("ROM file loaded into memory successfully.\n");

					// Automatically set PC to the address of the first instruction
					setPCToFirstInstruction();
					printerArea.append("PC set to the address of the first instruction.\n");

					displayLoadedROMContents();  // Display the file contents in the printer area
				} catch (IOException ex) {
					printerArea.append("Error loading ROM file: " + ex.getMessage() + "\n");
				}
			} else {
				printerArea.append("ROM file loading cancelled.\n");
			}
		}
	}

	// Load the selected ROM file into memory
	private void loadROMFile(java.io.File romFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(romFile));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] parts = line.trim().split("\\s+");  // Split by whitespace
			if (parts.length == 2) {
				try {
					// Parse the address as octal and convert it to decimal
					int address = Integer.parseInt(parts[0], 8);  // Octal to decimal (address)

					// Parse the instruction as octal and convert it to binary
					int instruction = Integer.parseInt(parts[1], 8);  // Octal to int (instruction in decimal)
					String binaryInstruction = Integer.toBinaryString(instruction);  // Convert to binary

					// Store the binary instruction in memory (keeping it as an integer representation of binary)
					if (address >= 0 && address < MEMORY_SIZE) {
						memory[address] = Integer.parseInt(binaryInstruction, 2);  // Store binary value as int
					}
				} catch (NumberFormatException ex) {
					printerArea.append("Invalid data in ROM file: " + line + "\n");
				}
			} else {
				printerArea.append("Malformed line in ROM file: " + line + "\n");
			}
		}
		reader.close();
	}

	// Automatically set the PC to the address of the first instruction
	private void setPCToFirstInstruction() {
		// Assuming the first instruction is located at the first non-zero memory location
		for (int i = 0; i < MEMORY_SIZE; i++) {
			if (memory[i] != 0) {
				PC = i;  // Set PC to the first instruction's address
				pcField.setText(String.valueOf(PC));  // Display the new PC value in the UI
				break;
			}
		}
	}

	// Display the loaded ROM contents in the printer output area
	private void displayLoadedROMContents() {
		printerArea.append("Memory Contents after ROM loading (addresses in decimal, instructions in binary):\n");
		for (int i = 0; i < MEMORY_SIZE; i++) {
			if (memory[i] != 0) {  // Display non-empty memory locations
				int decimalAddress = i;  // Address is already in decimal
				String instructionInBinary = Integer.toBinaryString(memory[i]);  // Instruction in binary
				printerArea.append("Address (Decimal): " + decimalAddress + ", Instruction (Binary): " + instructionInBinary + "\n");
			}
		}
	}

	// Step button action listener
	private class StepActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Do not allow stepping if the program is halted
			try {
				PC = Integer.parseInt(pcField.getText());  // Ensure PC is updated from user input
			} catch (NumberFormatException ex) {
				printerArea.append("Invalid PC value entered.\n");
				return;
			}
			if (pcField.getText().equals("HALT") && !pcField.isEditable()) {
				printerArea.append("Cannot step further. Program has halted.\n");
				return;
			}

			// Fetch and execute the instruction at the current PC
			if (PC < MEMORY_SIZE) {
				int instruction = memory[PC];  // Fetch instruction from memory
				executeInstruction(instruction);  // Decode and execute the instruction

				// Increment the PC only if it is not a HALT instruction
				if (!pcField.getText().equals("HALT")) {
					PC++;  // Increment PC after execution if it's not HALT
					pcField.setText(String.valueOf(PC));  // Update the PC field
				}
			}
		}
	}

	// Run button action listener
	private class RunActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Start running the program from the current PC
			while (PC < MEMORY_SIZE) {
				int instruction = memory[PC];  // Fetch the instruction from memory
				executeInstruction(instruction);  // Execute the instruction

				// If HALT is encountered, stop execution
				if (pcField.getText().equals("HALT")) {
					printerArea.append("Run stopped: Program halted at PC " + PC + ".\n");
					break;
				}

				// Increment PC for the next instruction
				PC++;
				pcField.setText(String.valueOf(PC));  // Update the PC field
			}
		}
	}

	// Execute a single instruction
	private void executeInstruction(int instruction) {
		// Ensure binary instruction is 16 bits
		String binaryInstruction = String.format("%16s", Integer.toBinaryString(instruction)).replace(' ', '0');
		if (binaryInstruction.equals("0000000000000000")) {
			// Full binary 0 indicates a HALT, so we stop execution
			printerArea.append("HLT: Program has been halted at PC " + PC + ".\n");
			stopProgram();  // Stop further execution, as HALT should end the program
			return;  // Exit early to avoid further execution
		}
		// Extract opcode (6 bits), GPR (2 bits), IXR (2 bits), I (1 bit), Address (5 bits)
		String opcode = binaryInstruction.substring(0, 6);  // First 6 bits as opcode
		int gprIndex = Integer.parseInt(binaryInstruction.substring(6, 8), 2);  // GPR index (2 bits)
		int ixrIndex = Integer.parseInt(binaryInstruction.substring(8, 10), 2);  // IXR index (2 bits)
		int iBit = Integer.parseInt(binaryInstruction.substring(10, 11));  // Indirect bit (1 bit)
		int address = Integer.parseInt(binaryInstruction.substring(11, 16), 2);  // Address (5 bits)

		int effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);

		// Handle opcode cases (including newly added instructions like JZ, JNE, JCC, etc.)
		switch (opcode) {
			case "001000":  // JZ - Jump if Zero
				if (GPR[gprIndex] == 0) {
					PC = effectiveAddress;
					printerArea.append("JZ: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is zero.\n");
				} else {
					PC++;
				}
				break;
			case "001001":  // JNE - Jump if Not Equal
				if (GPR[gprIndex] != 0) {
					PC = effectiveAddress;
					printerArea.append("JNE: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is not zero.\n");
				} else {
					PC++;
				}
				break;
			case "001010":  // JCC - Jump if Condition Code
				if (CC == gprIndex) {  // Assuming CC is stored as the Condition Code register
					PC = effectiveAddress;
					printerArea.append("JCC: Jumped to address " + effectiveAddress + " because Condition Code " + gprIndex + " is true.\n");
				} else {
					PC++;
				}
				break;
			case "001011":  // JMA - Unconditional Jump
				PC = effectiveAddress;
				printerArea.append("JMA: Jumped unconditionally to address " + effectiveAddress + ".\n");
				break;
			case "001100":  // JSR - Jump and Save Return Address
				GPR[3] = PC + 1;  // Save return address into GPR3 (as per description)
				PC = effectiveAddress;  // Jump to the effective address
				printerArea.append("JSR: Saved return address in GPR3 and jumped to address " + effectiveAddress + ".\n");
				break;
			case "001101":  // RFS - Return from Subroutine
				GPR[0] = address;  // Load immediate into GPR0
				PC = GPR[3];  // Set PC to the value in GPR3 (return address)
				printerArea.append("RFS: Returned to address stored in GPR3 and loaded immediate value " + address + " into GPR0.\n");
				break;
			case "001110":  // SOB - Subtract One and Branch
				GPR[gprIndex]--;
				if (GPR[gprIndex] > 0) {
					PC = effectiveAddress;
					printerArea.append("SOB: Decremented GPR " + gprIndex + " and jumped to address " + effectiveAddress + " because value > 0.\n");
				} else {
					PC++;
				}
				break;
			case "001111":  // JGE - Jump if Greater Than or Equal to
				if (GPR[gprIndex] >= 0) {
					PC = effectiveAddress;
					printerArea.append("JGE: Jumped to address " + effectiveAddress + " because GPR " + gprIndex + " is greater than or equal to zero.\n");
				} else {
					PC++;
				}
				break;
			case "000001":  // LDR - Load Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				GPR[gprIndex] = memory[effectiveAddress];  // Load value from memory into GPR
				gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
				printerArea.append("LDR: Loaded value from memory address " + effectiveAddress + " into GPR " + gprIndex + "\n");
				break;
			case "000010":  // STR - Store Register
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				memory[effectiveAddress] = GPR[gprIndex];  // Store GPR value into memory
				printerArea.append("STR: Stored value from GPR " + gprIndex + " into memory address " + effectiveAddress + "\n");
				break;
			case "000011":  // LDA - Load Register with Address
				GPR[gprIndex] = address;  // Load the address directly into the GPR
				gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));
				printerArea.append("LDA: Loaded address " + address + " into GPR " + gprIndex + "\n");
				break;
			case "100001":  // LDX - Load Index Register from Memory
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				IXR[ixrIndex - 1] = memory[effectiveAddress];  // Load value from memory into IXR
				ixrFields[ixrIndex - 1].setText(String.valueOf(IXR[ixrIndex - 1]));
				printerArea.append("LDX: Loaded value from memory address " + effectiveAddress + " into IXR " + ixrIndex + "\n");
				break;
			case "100010":  // STX - Store Index Register to Memory
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				memory[effectiveAddress] = IXR[ixrIndex - 1];  // Store IXR value into memory
				printerArea.append("STX: Stored value from IXR " + ixrIndex + " into memory address " + effectiveAddress + "\n");
				break;
			case "000000":
				effectiveAddress = calculateEffectiveAddress(ixrIndex, iBit, address);
				printerArea.append("Executed instruction at PC" + PC + "\n");
				break;
			default:
				printerArea.append("Unknown instruction.\n");
				MFR = 1;  // Set machine fault register
				mfrField.setText(String.valueOf(MFR));
				break;
		}
	}

	private void stopProgram() {
		// Logic to stop the machine execution, as the HALT instruction has been encountered
		pcField.setText("HALT");
		pcField.setEditable(true);
	}

	// Example helper function to calculate the effective address
	private int calculateEffectiveAddress(int ixrIndex, int iBit, int address) {
		int effectiveAddress = address;

		// If IXR is not 0, add the index register's value to the base address
		if (ixrIndex > 0) {
			effectiveAddress += IXR[ixrIndex - 1];  // IXR index 0-2 corresponds to IXR 1-3
		}

		// If indirect addressing is used, fetch the value at the effective address
		if (iBit == 1) {
			effectiveAddress = memory[effectiveAddress];
		}

		return effectiveAddress;
	}

	// Load button action listener (mimicking LDR)
	private class LoadActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int address = Integer.parseInt(marField.getText());  // Get the memory address from MAR
				int gprIndex = Integer.parseInt(octalInputField.getText());  // Get the GPR index from octalInputField

				// Load the value from memory into the specified GPR
				if (address >= 0 && address < MEMORY_SIZE && gprIndex >= 0 && gprIndex < 4) {
					GPR[gprIndex] = memory[address];  // Load value from memory into GPR
					gprFields[gprIndex].setText(String.valueOf(GPR[gprIndex]));  // Update GPR field in the UI
					printerArea.append("LDR: Loaded value " + memory[address] + " from memory address " + address + " into GPR " + gprIndex + ".\n");
				} else {
					printerArea.append("Invalid memory address or GPR index.\n");
				}
			} catch (NumberFormatException ex) {
				printerArea.append("Invalid input for memory address or GPR index.\n");
			}
		}
	}

	// Store button action listener (mimicking STR)
	private class StoreActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int address = Integer.parseInt(marField.getText());  // Get the memory address from MAR
				//int gprIndex = Integer.parseInt(octalInputField.getText());  // Get the GPR index from octalInputField
				int value = Integer.parseInt(mbrField.getText());
				// Store the value from the specified GPR into the given memory address
				if (address >= 0 && address < MEMORY_SIZE ) {
					memory[address] = value;  // Store value from GPR into memory
					printerArea.append("STR: Stored value " + value + " into memory address " + address + ".\n");
				} else {
					printerArea.append("Invalid memory address.\n");
				}
			} catch (NumberFormatException ex) {
				printerArea.append("Invalid input for memory address or value.\n");
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MachineSimulator1::new);
	}
}
