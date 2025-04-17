import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CacheMemory {
    public Map<String, String> cache;
    private final int capacity;
    private final Deque<String> keysOrder;

    // Constructor to initialize the MemoryCache
    public CacheMemory(int capacity) {
        this.capacity = capacity;
        this.cache = new ConcurrentHashMap<>(capacity);
        this.keysOrder = new LinkedList<>();
    }

    // Update key order as elements are added to the cache
    private void modifyKeyOrder(String key) {
        keysOrder.addLast(key);
        if (keysOrder.size() > capacity) {
            String oldestKey = keysOrder.removeFirst();
            cache.remove(oldestKey);
        }
    }

    // Return list of keys
    public String orderOfKeys() {
        return String.join(",", keysOrder);
    }

    // Add/update an element in the CacheMemory
    public void insertVal(String key, String value) {
        modifyKeyOrder(key);
        cache.put(key, value); // Updates value if key already exists
    }

    // Remove an element from the CacheMemory
    public void removeVal(String key) {
        cache.remove(key);
        keysOrder.remove(key);
    }

    // Get the value associated with a key in CacheMemory
    public String fetchVal(String key) {
        return cache.get(key);
    }
}
