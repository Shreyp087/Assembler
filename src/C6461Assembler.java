import java.io.*;
import java.util.*;

public class C6461Assembler {
    private Map<String, Integer> symbolTable = new HashMap<>();
    private List<String> sourceLines = new ArrayList<>();
    private int currentAddress = 0;

    // Instruction format constants
    private static final int OPCODE_BITS = 6;
    private static final int R_BITS = 2;
    private static final int IX_BITS = 2;
    private static final int I_BITS = 1;
    private static final int ADDRESS_BITS = 5;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java C6461Assembler <input.asm> <output.lst>");
            System.exit(1);
        }

        C6461Assembler assembler = new C6461Assembler();
        assembler.assemble(args[0], args[1]);
    }

    public void assemble(String inputFile, String outputFile) {
        firstPass(inputFile);
        secondPass(inputFile, outputFile);
    }

    private void firstPass(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith(";")) continue;

                sourceLines.add(line);
                String[] parts = line.split("[,\\s]+");

                if (parts[0].equals("LOC")) {
                    currentAddress = Integer.parseInt(parts[1]);
                }
                else if (line.contains(":")) {
                    String label = line.split(":")[0].trim();
                    symbolTable.put(label, currentAddress);
                    currentAddress++;
                }
                else if (parts[0].equals("Data")) {
                    currentAddress++;
                }
                else {
                    currentAddress++;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found: " + filename);
            System.exit(1);
        }
    }

    private void secondPass(String inputFile, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
             PrintWriter objWriter = new PrintWriter(new FileWriter("output.obj"))) {
            currentAddress = 0;

            for (String line : sourceLines) {
                String[] parts = line.split("[,\\s]+");

                if (parts[0].equals("LOC")) {
                    currentAddress = Integer.parseInt(parts[1]);
                    continue;
                }

                if (line.contains(":")) {
                    currentAddress++;
                    continue;
                }

                if (parts[0].equals("Data")) {
                    int value = parseValue(parts[1]);
                    writer.printf("%06o %06o %s\n",
                            currentAddress,
                            value,
                            line);
                    objWriter.println(String.format("%06o %06o", currentAddress, value));
                    currentAddress++;
                    continue;
                }

                String instruction = encodeInstruction(parts);
                writer.printf("%06o %s %s\n",
                        currentAddress,
                        instruction,
                        line);
                objWriter.println(String.format("%06o ", currentAddress) + instruction);
                currentAddress++;
            }
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
            System.exit(1);
        }
    }


//    private void secondPass(String inputFile, String outputFile) {
//        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
//             PrintWriter objWriter = new PrintWriter(new FileWriter("output.obj"))) { // Writing to output.obj
//
//            currentAddress = 0;
//
//            for (String line : sourceLines) {
//                String[] parts = line.split("[,\\s]+");
//
//                if (parts[0].equals("LOC")) {
//                    currentAddress = Integer.parseInt(parts[1]);
//                    continue;
//                }
//
//                if (line.contains(":")) {
//                    currentAddress++;
//                    continue;
//                }
//
//                if (parts[0].equals("Data")) {
//                    int value = parseValue(parts[1]);
//                    writer.printf("%06o %06o %s\n", currentAddress, value, line);
//                    objWriter.println(String.format("%06o", value));  // Output value as octal for output.obj
//                    currentAddress++;
//                    continue;
//                }
//
//                String instruction = encodeInstruction(parts);
//                writer.printf("%06o %s %s\n", currentAddress, instruction, line);
//                objWriter.println(instruction);  // Writing octal address to output.obj
//                currentAddress++;
//            }
//        } catch (IOException e) {
//            System.err.println("Error writing output file: " + e.getMessage());
//            System.exit(1);
//        }
//    }



    private String encodeInstruction(String[] parts) {
        String opcode = getOpcode(parts[0]);
        int r = 0, ix = 0, i = 0, address = 0;

        switch (parts[0]) {
            case "STR":
            case "LDA":
            case "JZ":
            case "JNE":
            case "LDR":
                r = Integer.parseInt(parts[1]);
                ix = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                address = parseValue(parts[3]);
                i = parts.length > 4 && parts[4].equals("1") ? 1 : 0;
                break;


            case "ADD":
            case "SUB":

            case "AND":
            case "OR":

                r = Integer.parseInt(parts[1]);
                ix = Integer.parseInt(parts[2]);
                address = parseValue(parts[3]);
                break;

            case "RRC":
                r = Integer.parseInt(parts[1]);
                int count = Integer.parseInt(parts[2]);
                address = (count << 1) | 0b1000; // L/R=1 (right), A/L=1 (logical)
                break;

            case "LDX":
            case "STX":
                ix = Integer.parseInt(parts[1]);
                address = parseValue(parts[2]);
                i = parts.length > 3 && parts[3].equals("I") ? 1 : 0;
                break;

            case "HLT":
                break;
        }

        String binary = String.format(
                "%6s%2s%2s%1s%5s",
                opcode,
                toBinary(r, R_BITS),
                toBinary(ix, IX_BITS),
                i,
                toBinary(address, ADDRESS_BITS)
        ).replace(' ', '0');

        return binaryToOctal(binary);
    }

    private String getOpcode(String mnemonic) {
        switch (mnemonic.toUpperCase()) {
            // Load/Store Instructions (Part 0)
            case "LDR": return "000001";  // 01 octal
            case "STR": return "000010";  // 02 octal
            case "LDA": return "000011";  // 03 octal
            case "LDX": return "100001";  // 41 octal
            case "STX": return "100010";  // 42 octal

            // Transfer Instructions (Part II)
            case "JZ":  return "001000";  // 10 octal
            case "JNE": return "001001";  // 11 octal
            case "JCC": return "001010";  // 12 octal
            case "JMA": return "001011";  // 13 octal
            case "JSR": return "001100";  // 14 octal
            case "RFS": return "001101";  // 15 octal
            case "SOB": return "001110";  // 16 octal
            case "JGE": return "001111";  // 17 octal

            // Arithmetic/Logical (Part II)
            case "AMR": return "000100";  // 04 octal
            case "SMR": return "000101";  // 05 octal
            case "AIR": return "000110";  // 06 octal
            case "SIR": return "000111";  // 07 octal
            case "MLT": return "111000";  // 70 octal
            case "DVD": return "111001";  // 71 octal
            case "TRR": return "111010";  // 72 octal
            case "AND": return "111011";  // 73 octal
            case "ORR": return "111100";  // 74 octal
            case "NOT": return "111101";  // 75 octal
            case "ADD": return "000100"; // 04 octal - Arithmetic Add
            case "SUB": return "000101";  // SMR: Octal 05
            case "OR":  return "111100";  // ORR: Octal 74



            // Shift/Rotate (Part II)
            case "SRC": return "011001";  // 31 octal
            case "RRC": return "011010";  // 32 octal

            // I/O Operations (Part III)
            case "IN":  return "110001";  // 61 octal
            case "OUT": return "110010";  // 62 octal
            case "CHK": return "110011";  // 63 octal

            // Floating Point/Vector (Part IV)
            case "FADD":   return "011011";  // 33 octal
            case "FSUB":   return "011100";  // 34 octal
            case "VADD":   return "011101";  // 35 octal
            case "VSUB":   return "011110";  // 36 octal
            case "CNVRT":  return "011111";  // 37 octal
            case "LDFR":   return "101000";  // 50 octal
            case "STFR":   return "101001";  // 51 octal

            // Miscellaneous
            case "HLT":  return "000000";  // 00 octal
            case "TRAP": return "110000";  // 30 octal


            default: throw new IllegalArgumentException("Invalid instruction: " + mnemonic);        }
    }

    private int parseValue(String value) {
        if (value.matches("[A-Za-z]+")) {
            return symbolTable.getOrDefault(value, 0);
        }
        return Integer.parseInt(value);
    }

    private String toBinary(int value, int bits) {
        return String.format("%" + bits + "s", Integer.toBinaryString(value))
                .replace(' ', '0');
    }

    private String binaryToOctal(String binary) {
        int decimal = Integer.parseInt(binary, 2);
        return String.format("%06o", decimal);
    }
}