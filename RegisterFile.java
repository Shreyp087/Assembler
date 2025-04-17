import java.util.*;

public class RegisterFile {

    // Register array and register size
    private int[] val;
    private int size;

    public RegisterFile(int size) {
        init(size);
    }

    // Getter & Setter for register values
    public int[] getRegisterVals() {
        return val;
    }

    public void setRegisterVals(int[] newVal) {
        // Ensure the register size is non-zero before setting values
        if (size == 0) {
            throw new IllegalStateException("Register size is zero. Cannot set value.");
        }
        this.val = Arrays.copyOf(newVal, newVal.length);
    }

    // Initialize register values and size
    private void init(int size) {
        this.size = size;
        this.val = new int[size]; // Allocate array for register values
    }
}
