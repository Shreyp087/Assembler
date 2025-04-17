import java.util.*;

// Manages a 2D array of integer values.
public class MemoryUnit {
   private final int[][] values = new int[2048][16];

   public MemoryUnit() {
   } // Constructor

   public int[] getMemoryVals(int index) {
      return Arrays.copyOf(values[index], 16);
   }

   public void setMemoryVals(int index, int[] data) {
      System.arraycopy(data, 0, values[index], 0, 16);
   }
}
