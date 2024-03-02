import java.util.HashMap;
import java.util.Map;

public class Memory {
    static private final Map<String, Object> Store = new HashMap<>();
    static private final Map<String, Long> Expiry = new HashMap<>();

    public static void set(String key, Object value) {
        Store.put(key, value);
    }

    public static void set(String key, Object value, int expiresInMS) {
        // add 50ms buffer
        long expiresAt = System.currentTimeMillis() + expiresInMS + 50;
        Expiry.put(key, expiresAt);
        set(key, value);
    }

    public static Object get(String key) {
        if (Expiry.containsKey(key) && keyHasExpired(key)) {
            Store.remove(key);
            Expiry.remove(key);
            return null;
        }
        return Store.get(key);
    }

    private static boolean keyHasExpired(String key) {
        return System.currentTimeMillis() < Expiry.get(key);
    }
}
