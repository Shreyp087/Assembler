import javax.swing.*;
import java.io.*;
import java.util.*;

public class Core {
    // Create objects for memory and registers
    InstructionFormatter strFormatter = new InstructionFormatter();
    MemoryUnit dtMem = new MemoryUnit();

    // Add the main registers in Processor (CPU)
    RegisterFile PC = new RegisterFile(12);
    RegisterFile CC = new RegisterFile(4);
    RegisterFile IR = new RegisterFile(16);
    RegisterFile MAR = new RegisterFile(12);
    RegisterFile MBR = new RegisterFile(16);
    RegisterFile MFR = new RegisterFile(4);
    RegisterFile IX1 = new RegisterFile(16);
    RegisterFile IX2 = new RegisterFile(16);
    RegisterFile IX3 = new RegisterFile(16);
    RegisterFile GPR0 = new RegisterFile(16);
    RegisterFile GPR1 = new RegisterFile(16);
    RegisterFile GPR2 = new RegisterFile(16);
    RegisterFile GPR3 = new RegisterFile(16);
    RegisterFile HLT = new RegisterFile(1);
    public boolean fileOpened = false; // Indicates whether the file has been successfully opened or not.
    public boolean fileOpened2 = false;

    // Function to execute a Single Step or Run

    public void execute(String type) {
        if ("single".equals(type)) {
            int[] instruction_address = getRegisterVals("PC");
            int int_instruction_address = strFormatter.binToInt(instruction_address);
            setRegisterVals("IR", getMemoryVals(int_instruction_address));

            // Increment PC
            int[] current_PC = getRegisterVals("PC");
            int int_PC = strFormatter.binToInt(current_PC);
            int_PC = int_PC + 1;
            int[] new_PC = intToBinaryArrayShort(Integer.toBinaryString(int_PC));
            setRegisterVals("PC", new_PC);

            // Read and decode instruction
            int[] binaryInstruction = getMemoryVals(int_instruction_address);
            int[] OpCode = Arrays.copyOfRange(binaryInstruction, 0, 6);
            String instruction = decodeOPCode(OpCode);

            // Execute opcode instruction
            if ("LDR".equals(instruction)) { // Load Opcode LDR

                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int I = result[1];
                int R = result[2];
                int IX = result[3];

                // Setting MAR to the location in memory to fetch
                int Addr = result[4];
                setRegisterVals("MAR", intToBinaryArrayShort(Integer.toBinaryString(EA)));

                // Set MBR to the value to be stored in register
                switch (R) {
                    case 0:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        GPR0.setRegisterVals(MBR.getRegisterVals());
                        break;
                    case 1:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        GPR1.setRegisterVals(MBR.getRegisterVals());
                        break;
                    case 2:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        GPR2.setRegisterVals(MBR.getRegisterVals());
                        break;
                    default:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        GPR3.setRegisterVals(MBR.getRegisterVals());
                }
                setRegisterVals("MBR", getMemoryVals(EA));

            } else if ("STR".equals(instruction)) { // Load Opcode STR

                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int I = result[1];
                int R = result[2];
                int IX = result[3];
                int Addr = result[4];

                // Set MBR to the value to be stored in memory
                setRegisterVals("MAR", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                switch (R) {
                    case 0:
                        setRegisterVals("MBR", GPR0.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    case 1:
                        setRegisterVals("MBR", GPR1.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    case 2:
                        setRegisterVals("MBR", GPR2.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    default:
                        setRegisterVals("MBR", GPR3.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                }
            } else if ("LDA".equals(instruction)) { // Load Opcode LDA
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int I = result[1];
                int R = result[2];
                int IX = result[3];
                int Addr = result[4];

                setRegisterVals("MAR", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                int[] converted_value = intToBinaryArray(Integer.toBinaryString(EA));
                switch (R) {
                    case 0:
                        GPR0.setRegisterVals(converted_value);
                        break;
                    case 1:
                        GPR1.setRegisterVals(converted_value);
                        break;
                    case 2:
                        GPR2.setRegisterVals(converted_value);
                        break;
                    default:
                        GPR3.setRegisterVals(converted_value);
                }
            } else if ("LDX".equals(instruction)) { // Load Opcode LDX
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int I = result[1];
                int R = result[2];
                int IX = result[3];
                int Addr = result[4];

                setRegisterVals("MAR", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                switch (IX) {
                    case 1:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        IX1.setRegisterVals(MBR.getRegisterVals());
                        break;
                    case 2:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        IX2.setRegisterVals(MBR.getRegisterVals());
                        break;
                    case 3:
                        setRegisterVals("MBR", getMemoryVals(EA));
                        IX2.setRegisterVals(MBR.getRegisterVals());
                        break;
                    default:
                }
            } else if ("STX".equals(instruction)) { // Load Opcode STX
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int I = result[1];
                int R = result[2];
                int IX = result[3];
                int Addr = result[4];

                setRegisterVals("MAR", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                switch (IX) {
                    case 1:
                        setRegisterVals("MBR", IX1.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    case 2:
                        setRegisterVals("MBR", IX2.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    case 3:
                        setRegisterVals("MBR", IX3.getRegisterVals());
                        setMemoryVals(EA, MBR.getRegisterVals());
                        break;
                    default:
                }
            } else if ("HLT".equals(instruction)) { // Load Opcode HLT
                int[] msg = new int[] { 1 };
                HLT.setRegisterVals(msg);
            } else if ("IN".equals(instruction)) { // Input character from keyboard
                int[] result = computeEA(binaryInstruction);
                int R = result[2]; // Get register number

                // Read character from keyboard and convert to binary
                String input = JOptionPane.showInputDialog("Enter a character:");
                if (input != null && input.length() > 0) {
                    int value = (int) input.charAt(0);
                    int[] binary = intToBinaryArray(Integer.toBinaryString(value));
                    // Store in specified register
                    switch (R) {
                        case 0:
                            GPR0.setRegisterVals(binary);
                            break;
                        case 1:
                            GPR1.setRegisterVals(binary);
                            break;
                        case 2:
                            GPR2.setRegisterVals(binary);
                            break;
                        default:
                            GPR3.setRegisterVals(binary);
                    }
                }

            } else if ("OUT".equals(instruction)) { // Output character to console
                int[] result = computeEA(binaryInstruction);
                int R = result[2];

                // Get value from register and convert to character
                int[] binary;
                switch (R) {
                    case 0:
                        binary = GPR0.getRegisterVals();
                        break;
                    case 1:
                        binary = GPR1.getRegisterVals();
                        break;
                    case 2:
                        binary = GPR2.getRegisterVals();
                        break;
                    default:
                        binary = GPR3.getRegisterVals();
                }
                int value = strFormatter.binToInt(binary);
                System.out.print((char) value);

            } else if ("ADD".equals(instruction)) { // Add
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                // Add memory value to register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 + memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 + memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 + memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 + memVal)));
                }

            } else if ("SUB".equals(instruction)) { // Subtract
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                // Subtract memory value from register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 - memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 - memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 - memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 - memVal)));
                }

            } else if ("MUL".equals(instruction)) { // Multiply
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                // Multiply register by memory value
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 * memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 * memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 * memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 * memVal)));
                }

            } else if ("DIV".equals(instruction)) { // Divide
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                if (memVal == 0) {
                    // Handle divide by zero
                    int[] fault_code = { 0, 0, 1, 0 };
                    MFR.setRegisterVals(fault_code);
                    int[] msg = { 1 };
                    HLT.setRegisterVals(msg);
                    return;
                }

                // Divide register by memory value
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 / memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 / memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 / memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 / memVal)));
                }

            } else if ("AND".equals(instruction)) { // Logical AND
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];
                int IX = result[3];

                int[] register2 = new int[16];
                switch (IX) {
                    case 1:
                        register2 = IX1.getRegisterVals();
                        break;
                    case 2:
                        register2 = IX2.getRegisterVals();
                        break;
                    default:
                        register2 = IX3.getRegisterVals();
                }

                // Perform bitwise AND
                switch (R) {
                    case 0:
                        int[] regVal0 = GPR0.getRegisterVals();
                        int[] newVal0 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal0[i] = regVal0[i] & register2[i];
                        }
                        GPR0.setRegisterVals(newVal0);
                        break;
                    case 1:
                        int[] regVal1 = GPR1.getRegisterVals();
                        int[] newVal1 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal1[i] = regVal1[i] & register2[i];
                        }
                        GPR1.setRegisterVals(newVal1);
                        break;
                    case 2:
                        int[] regVal2 = GPR2.getRegisterVals();
                        int[] newVal2 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal2[i] = regVal2[i] & register2[i];
                        }
                        GPR2.setRegisterVals(newVal2);
                        break;
                    default:
                        int[] regVal3 = GPR3.getRegisterVals();
                        int[] newVal3 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal3[i] = regVal3[i] & register2[i];
                        }
                        GPR3.setRegisterVals(newVal3);
                }

            } else if ("ORR".equals(instruction)) { // Logical OR
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];
                int IX = result[3];
                int[] register2 = new int[16];
                switch (IX) {
                    case 0:
                        register2 = IX1.getRegisterVals();
                        break;
                    case 1:
                        register2 = IX2.getRegisterVals();
                        break;
                    default:
                        register2 = IX3.getRegisterVals();
                }

                // Perform bitwise OR
                switch (R) {
                    case 0:
                        int[] regVal0 = GPR0.getRegisterVals();
                        int[] newVal0 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal0[i] = regVal0[i] | register2[i];
                        }
                        GPR0.setRegisterVals(newVal0);
                        break;
                    case 1:
                        int[] regVal1 = GPR1.getRegisterVals();
                        int[] newVal1 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal1[i] = regVal1[i] | register2[i];
                        }
                        GPR1.setRegisterVals(newVal1);
                        break;
                    case 2:
                        int[] regVal2 = GPR2.getRegisterVals();
                        int[] newVal2 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal2[i] = regVal2[i] | register2[i];
                        }
                        GPR2.setRegisterVals(newVal2);
                        break;
                    default:
                        int[] regVal3 = GPR3.getRegisterVals();
                        int[] newVal3 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal3[i] = regVal3[i] | register2[i];
                        }
                        GPR3.setRegisterVals(newVal3);
                }

            } else if ("NOT".equals(instruction)) { // Logical NOT
                int[] result = computeEA(binaryInstruction);
                int R = result[2];

                // Perform bitwise NOT on register
                switch (R) {
                    case 0:
                        int[] regVal0 = GPR0.getRegisterVals();
                        int[] newVal0 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal0[i] = regVal0[i] == 0 ? 1 : 0;
                        }
                        GPR0.setRegisterVals(newVal0);
                        break;
                    case 1:
                        int[] regVal1 = GPR1.getRegisterVals();
                        int[] newVal1 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal1[i] = regVal1[i] == 0 ? 1 : 0;
                        }
                        GPR1.setRegisterVals(newVal1);
                        break;
                    case 2:
                        int[] regVal2 = GPR2.getRegisterVals();
                        int[] newVal2 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal2[i] = regVal2[i] == 0 ? 1 : 0;
                        }
                        GPR2.setRegisterVals(newVal2);
                        break;
                    default:
                        int[] regVal3 = GPR3.getRegisterVals();
                        int[] newVal3 = new int[16];
                        for (int i = 0; i < 16; i++) {
                            newVal3[i] = regVal3[i] == 0 ? 1 : 0;
                        }
                        GPR3.setRegisterVals(newVal3);
                }

            } else if ("JZ".equals(instruction)) { // Jump if Zero
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                // Check if register is zero
                int[] regValue;
                switch (R) {
                    case 0:
                        regValue = GPR0.getRegisterVals();
                        break;
                    case 1:
                        regValue = GPR1.getRegisterVals();
                        break;
                    case 2:
                        regValue = GPR2.getRegisterVals();
                        break;
                    default:
                        regValue = GPR3.getRegisterVals();
                }

                if (strFormatter.binToInt(regValue) == 0) {
                    setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                }

            } else if ("JNE".equals(instruction)) { // Jump if Not Equal
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                // Check if register is not zero
                int[] regValue;
                switch (R) {
                    case 0:
                        regValue = GPR0.getRegisterVals();
                        break;
                    case 1:
                        regValue = GPR1.getRegisterVals();
                        break;
                    case 2:
                        regValue = GPR2.getRegisterVals();
                        break;
                    default:
                        regValue = GPR3.getRegisterVals();
                }

                if (strFormatter.binToInt(regValue) != 0) {
                    setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                }

            } else if ("JCC".equals(instruction)) { // Jump if Condition Code
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2]; // Using R as condition code

                // Get condition code
                int[] cc = CC.getRegisterVals();
                if (cc[R] == 1) {
                    setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                }
            } else if ("AMR".equals(instruction)) { // Add Memory To Register
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                // Add memory value to register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 + memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 + memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 + memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 + memVal)));
                }
            }

            else if ("SMR".equals(instruction)) { // Subtract Memory From Register
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                int[] memoryValue = getMemoryVals(EA);
                int memVal = strFormatter.binToInt(memoryValue);

                // Subtract memory value from register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 - memVal)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 - memVal)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 - memVal)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 - memVal)));
                }
            }

            else if ("AIR".equals(instruction)) { // Add Immediate to Register
                int[] result = computeEA(binaryInstruction);
                int immed = result[4]; // Using Address field as immediate value
                int R = result[2];

                // Add immediate value to register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 + immed)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 + immed)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 + immed)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 + immed)));
                }
            }

            else if ("SIR".equals(instruction)) { // Subtract Immediate from Register
                int[] result = computeEA(binaryInstruction);
                int immed = result[4]; // Using Address field as immediate value
                int R = result[2];

                // Subtract immediate value from register
                switch (R) {
                    case 0:
                        int regVal0 = strFormatter.binToInt(GPR0.getRegisterVals());
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal0 - immed)));
                        break;
                    case 1:
                        int regVal1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal1 - immed)));
                        break;
                    case 2:
                        int regVal2 = strFormatter.binToInt(GPR2.getRegisterVals());
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal2 - immed)));
                        break;
                    default:
                        int regVal3 = strFormatter.binToInt(GPR3.getRegisterVals());
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(regVal3 - immed)));
                }
            }

            else if ("JMA".equals(instruction)) { // Unconditional Jump To Address
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
            }

            else if ("JSR".equals(instruction)) { // Jump and Save Return Address
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                // Save return address (current PC + 1) in R3
                int[] currentPC = getRegisterVals("PC");
                int nextInstr = strFormatter.binToInt(currentPC);
                GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(nextInstr)));
                setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
            }

            else if ("RFS".equals(instruction)) { // Return From Subroutine
                int[] result = computeEA(binaryInstruction);
                int R = result[2];
                int[] returnAddr = GPR3.getRegisterVals();
                setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(strFormatter.binToInt(returnAddr))));
            }

            else if ("SOB".equals(instruction)) { // Subtract One and Branch
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                // Subtract one from register
                int[] regValue;
                switch (R) {
                    case 0:
                        regValue = GPR0.getRegisterVals();
                        int val0 = strFormatter.binToInt(regValue) - 1;
                        GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(val0)));
                        if (val0 > 0) {
                            setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                        }
                        break;
                    case 1:
                        regValue = GPR1.getRegisterVals();
                        int val1 = strFormatter.binToInt(regValue) - 1;
                        GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(val1)));
                        if (val1 > 0) {
                            setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                        }
                        break;
                    case 2:
                        regValue = GPR2.getRegisterVals();
                        int val2 = strFormatter.binToInt(regValue) - 1;
                        GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(val2)));
                        if (val2 > 0) {
                            setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                        }
                        break;
                    default:
                        regValue = GPR3.getRegisterVals();
                        int val3 = strFormatter.binToInt(regValue) - 1;
                        GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(val3)));
                        if (val3 > 0) {
                            setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                        }
                }
            }

            else if ("JGE".equals(instruction)) { // Jump Greater Than or Equal To
                int[] result = computeEA(binaryInstruction);
                int EA = result[0];
                int R = result[2];

                // Check if register value is >= 0
                int[] regValue;
                switch (R) {
                    case 0:
                        regValue = GPR0.getRegisterVals();
                        break;
                    case 1:
                        regValue = GPR1.getRegisterVals();
                        break;
                    case 2:
                        regValue = GPR2.getRegisterVals();
                        break;
                    default:
                        regValue = GPR3.getRegisterVals();
                }

                if (strFormatter.binToInt(regValue) >= 0) {
                    setRegisterVals("PC", intToBinaryArrayShort(Integer.toBinaryString(EA)));
                }
            }

            else if ("MLT".equals(instruction)) { // Multiply Register by Register
                int[] result = computeEA(binaryInstruction);
                int Rx = result[2]; // First register
                int IX = result[3]; // second register
                int[] register2 = new int[16];
                switch (IX) {
                    case 1:
                        register2 = IX1.getRegisterVals();
                        break;
                    case 2:
                        register2 = IX2.getRegisterVals();
                        break;
                    default:
                        register2 = IX3.getRegisterVals();
                }

                // Get values from both registers
                int val1, val2;
                val2 = strFormatter.binToInt(register2);
                switch (Rx) {
                    case 0:
                        val1 = strFormatter.binToInt(GPR0.getRegisterVals());
                        break;
                    case 1:
                        val1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        break;
                    case 2:
                        val1 = strFormatter.binToInt(GPR2.getRegisterVals());
                        break;
                    default:
                        val1 = strFormatter.binToInt(GPR3.getRegisterVals());
                }
                int multResult = val1 * val2;
                // Store high order bits in Rx, low order bits in Rx+1
                if (Rx < 3) {
                    switch (Rx) {
                        case 0:
                            GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                        case 1:
                            GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                        case 2:
                            GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                    }
                } else {
                    // Set MFR for illegal operation if Rx is 3
                    int[] fault_code = { 0, 1, 0, 0 };
                    MFR.setRegisterVals(fault_code);
                }
            } else if ("DVD".equals(instruction)) { // Multiply Register by Register
                int[] result = computeEA(binaryInstruction);
                int Rx = result[2]; // First register
                int IX = result[3]; // second register
                int[] register2 = new int[16];
                switch (IX) {
                    case 1:
                        register2 = IX1.getRegisterVals();
                        break;
                    case 2:
                        register2 = IX2.getRegisterVals();
                        break;
                    default:
                        register2 = IX3.getRegisterVals();
                }

                // Get values from both registers
                int val1, val2;
                val2 = strFormatter.binToInt(register2);
                switch (Rx) {
                    case 0:
                        val1 = strFormatter.binToInt(GPR0.getRegisterVals());
                        break;
                    case 1:
                        val1 = strFormatter.binToInt(GPR1.getRegisterVals());
                        break;
                    case 2:
                        val1 = strFormatter.binToInt(GPR2.getRegisterVals());
                        break;
                    default:
                        val1 = strFormatter.binToInt(GPR3.getRegisterVals());
                }
                int multResult = val1 / val2;
                // Store high order bits in Rx, low order bits in Rx+1
                if (Rx < 3) {
                    switch (Rx) {
                        case 0:
                            GPR0.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                        case 1:
                            GPR1.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                        case 2:
                            GPR2.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult >> 16)));
                            GPR3.setRegisterVals(intToBinaryArray(Integer.toBinaryString(multResult & 0xFFFF)));
                            break;
                    }
                } else {
                    // Set MFR for illegal operation if Rx is 3
                    int[] fault_code = { 0, 1, 0, 0 };
                    MFR.setRegisterVals(fault_code);
                }
            }
        }
    }

    // Function to decode a operation instruction
    public String decodeOPCode(int[] binary_OPCode) {
        String returnVal;
        // Convert int array to string
        String opCode = Arrays.toString(binary_OPCode);
        opCode = opCode.replace("[", "");
        opCode = opCode.replace("]", "");
        opCode = opCode.replace(",", "");
        opCode = opCode.replace(" ", "");

        returnVal = switch (opCode) {
            case "000001" -> "LDR"; // Load Register From Memory
            case "000010" -> "STR"; // Store Register To Memory
            case "000011" -> "LDA"; // Load Register with Address
            case "000100" -> "AMR"; // Add Memory To Register
            case "000101" -> "SMR"; // Subtract Memory From Register
            case "000110" -> "AIR"; // Add Immediate to Register
            case "000111" -> "SIR"; // Subtract Immediate from Register
            case "001000" -> "JZ"; // Jump If Zero
            case "001001" -> "JNE"; // Jump If Not Equal
            case "001010" -> "JCC"; // Jump If Condition Code
            case "001011" -> "JMA"; // Unconditional Jump To Address
            case "001100" -> "JSR"; // Jump and Save Return Address
            case "001101" -> "RFS"; // Return From Subroutine
            case "001110" -> "SOB"; // Subtract One and Branch
            case "001111" -> "JGE"; // Jump Greater Than or Equal To
            case "010000" -> "MLT"; // Multiply Register by Register
            case "010001" -> "DVD"; // Divide Register by Register
            case "010011" -> "AND"; // Logical And of Register and Register
            case "010100" -> "ORR"; // Logical Or of Register and Register
            case "010101" -> "NOT"; // Logical Not of Register
            case "011000" -> "IN"; // Input Character To Register
            case "011001" -> "OUT"; // Output Character To Console
            case "100001" -> "LDX"; // Load Index Register from Memory
            case "100010" -> "STX"; // Store Index Register to Memory
            default -> "HLT";
        };
        return returnVal;
    }

    // Function to get values from the registers
    public int[] getRegisterVals(String register) {
        return switch (register) {
            case "PC" -> PC.getRegisterVals();
            case "CC" -> CC.getRegisterVals();
            case "IR" -> IR.getRegisterVals();
            case "MAR" -> MAR.getRegisterVals();
            case "MBR" -> MBR.getRegisterVals();
            case "MFR" -> MFR.getRegisterVals();
            case "IX1" -> IX1.getRegisterVals();
            case "IX2" -> IX2.getRegisterVals();
            case "IX3" -> IX3.getRegisterVals();
            case "GPR0" -> GPR0.getRegisterVals();
            case "GPR1" -> GPR1.getRegisterVals();
            case "GPR2" -> GPR2.getRegisterVals();
            case "GPR3" -> GPR3.getRegisterVals();
            case null, default -> HLT.getRegisterVals();
        };
    }

    // Function to set value to the registers
    public void setRegisterVals(String register, int[] value) {
        switch (register) {
            case "PC" -> {
                if (strFormatter.binToInt(value) < 10) {
                    // If PC can't be less than 10!!
                    int[] tmp_val = { 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0 };
                    PC.setRegisterVals(tmp_val);
                } else {
                    PC.setRegisterVals(value);
                }
            }
            case "CC" -> CC.setRegisterVals(value);
            case "IR" -> IR.setRegisterVals(value);
            case "MAR" -> MAR.setRegisterVals(value);
            case "MBR" -> MBR.setRegisterVals(value);
            case "MFR" -> MFR.setRegisterVals(value);
            case "IX1" -> IX1.setRegisterVals(value);
            case "IX2" -> IX2.setRegisterVals(value);
            case "IX3" -> IX3.setRegisterVals(value);
            case "GPR0" -> GPR0.setRegisterVals(value);
            case "GPR1" -> GPR1.setRegisterVals(value);
            case "GPR2" -> GPR2.setRegisterVals(value);
            case "GPR3" -> GPR3.setRegisterVals(value);
            case null, default -> HLT.setRegisterVals(value);
        }

    }

    // Function to get a value from memory
    public int[] getMemoryVals(int row) {
        if (row < 6) {
            return new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

        } else {
            return dtMem.getMemoryVals(row);
        }
    }

    // Function to set a value to memory
    public void setMemoryVals(int row, int[] value) {
        if (row < 6) {
            int[] fault_code = new int[] { 0, 0, 0, 1 };
            MFR.setRegisterVals(fault_code);
            int[] msg = new int[] { 1 };
            HLT.setRegisterVals(msg);
        } else {
            dtMem.setMemoryVals(row, value);
        }
    }

    public int[] computeEA(int[] instruction) {
        // Formatting data from the Array
        String strInstruction = Arrays.toString(instruction)
                .replace("[", "")
                .replace("]", "")
                .replace(",", "")
                .replace(" ", "");

        // Calculate I (Indirect Addressing Mode)
        int I = (strInstruction.charAt(10) == '0') ? 0 : 1;
        // Calculate R (General Register)
        int R = calculateRegister(strInstruction.substring(6, 8));
        // Calculate IX (Index Register)
        int IX = calculateRegister(strInstruction.substring(8, 10));
        // Calculate Address Field
        int[] Addr_Field = Arrays.copyOfRange(instruction, 11, 16);
        // Calculate Effective Address (EA)
        int EA;
        if (I == 0) {
            EA = (IX == 0) ? strFormatter.binToInt(Addr_Field) : strFormatter.binToInt(Addr_Field) + getIXValue(IX);
        } else {
            EA = calculateIndirectEA(Addr_Field, IX);
        }
        return new int[] { EA, I, R, IX, strFormatter.binToInt(Addr_Field) };
    }

    private int calculateRegister(String binaryString) {
        return switch (binaryString) {
            case "00" -> 0;
            case "01" -> 1;
            case "10" -> 2;
            case "11" -> 3;
            default -> 0; // Fallback in case of unexpected input
        };
    }

    private int getIXValue(int IX) {
        String IXRegister = (IX == 1) ? "IX1" : (IX == 2) ? "IX2" : "IX3";
        return strFormatter.binToInt(getRegisterVals(IXRegister));
    }

    private int calculateIndirectEA(int[] Addr_Field, int IX) {
        int tempVariable = strFormatter.binToInt(Addr_Field);
        if (IX != 0) {
            int IX_value = getIXValue(IX);
            tempVariable += IX_value;
        }
        // Get memory value at the calculated address
        int[] tmp_var = getMemoryVals(tempVariable);
        tempVariable = strFormatter.binToInt(tmp_var);
        // Get the final EA from memory
        return strFormatter.binToInt(getMemoryVals(tempVariable));
    }

    // Function to load the file into memory
    public void loadIPLFile(String path) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new DataInputStream(new FileInputStream(path))))) {
            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] tokens = strLine.split(" ");
                // ** If loading.txt file is in hexadecimal format, use strFormatter.hexToInt()
                // instead of strFormatter.octToInt() ** //
                int row = strFormatter.octToInt(tokens[0]);
                // ** If loading.txt file is hexadecimal, use hexToBinaryArrayShort(tokens[0])
                // instead of strFormatter.octToBinaryArr(tokens[0],12); ** //
                int[] rowBinary = strFormatter.octToBinArr(tokens[0], 12);
                setRegisterVals("MAR", rowBinary);
                // ** If loading.txt file is hexadecimal, use
                // strFormatter.hexToBinaryArray(tokens[1]) instead of
                // strFormatter.octToBinaryArr(tokens[1],16) ** //
                int[] value = strFormatter.octToBinArr(tokens[1], 16);
                setRegisterVals("MBR", value);

                // Set memory value
                System.out.println("Setting Memory for Row " + row + " \n");
                setMemoryVals(row, value);
                int[] fault_code = { 0, 0, 0, 0 };
                MFR.setRegisterVals(fault_code);
            }
        }
    }

    public void openFileChooser() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null); // No parent frame

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                loadIPLFile(selectedFile.getAbsolutePath());
                if (selectedFile.getAbsolutePath().contains("Program1")) {
                    fileOpened = true;
                }
                if (selectedFile.getAbsolutePath().contains("Program2")) {
                    fileOpened2 = true;
                }
            } catch (IOException ex) {
                System.out.println("Something went wrong while loading the file" + ex);
            }
            int[] default_PC_loc = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0 };
            setRegisterVals("PC", default_PC_loc);
        } else {
            System.out.println("Something went wrong while opening the dialog box");
        }
    }

    // Function to convert a binary int value to binary array value.
    public int[] intToBinaryArray(String int_value) {
        int[] returnVal = new int[16];
        char[] arr = int_value.toCharArray();
        for (int i = 0; i < 16; i++) {
            if (i < 16 - arr.length) {
                returnVal[i] = 0;
            } else {
                returnVal[i] = Character.getNumericValue(int_value.charAt(i - (16 - arr.length)));
            }
        }
        return returnVal;
    }

    // Function to convert a binary int value to binary array value specifically for
    // the PC
    public int[] intToBinaryArrayShort(String int_value) {
        int[] returnVal = new int[12];
        char[] arr = int_value.toCharArray();
        for (int i = 0; i < 12; i++) {
            if (i < 12 - arr.length) {
                returnVal[i] = 0;
            } else {
                returnVal[i] = Character.getNumericValue(int_value.charAt(i - (12 - arr.length)));
            }

        }
        return returnVal;
    }
}
