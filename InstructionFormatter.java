public class InstructionFormatter {

    public int binToInt(int[] binary) {
        StringBuilder binaryString = new StringBuilder();
        for (int bit : binary) {
            binaryString.append(bit);
        }
        return Integer.parseInt(binaryString.toString(), 2);
    }

    public int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public int octToInt(String oct) {
        return Integer.parseInt(oct, 8);
    }

    private int[] hexToBinArr(String hex, int length) {
        int decimalValue = Integer.parseInt(hex, 16);
        String binaryString = String.format("%" + length + "s", Integer.toBinaryString(decimalValue)).replace(' ', '0');
        int[] binaryArray = new int[length];
        for (int i = 0; i < length; i++) {
            binaryArray[i] = Character.getNumericValue(binaryString.charAt(i));
        }
        return binaryArray;
    }

    int[] octToBinArr(String octal, int length) {
        int decimalValue = Integer.parseInt(octal, 8);
        String binaryString = String.format("%" + length + "s", Integer.toBinaryString(decimalValue)).replace(' ', '0');
        int[] binaryArray = new int[length];
        for (int i = 0; i < length; i++) {
            binaryArray[i] = Character.getNumericValue(binaryString.charAt(i));
        }
        return binaryArray;
    }

    public int[] hexToBinArr(String hex) {
        return hexToBinArr(hex, 16);
    }

    // Wrapper for 12 bits (for MAR)
    public int[] hexToBinArrShort(String hex) {
        return hexToBinArr(hex, 12);
    }

    public int[] intToBinaryArray(int value) {
        int[] binaryArray = new int[16];

        for (int i = 15; i >= 0; i--) {
            binaryArray[i] = (value & 1); // Extract the least significant bit
            value >>= 1; // Shift value right by 1 bit
        }

        return binaryArray;
    }

}
