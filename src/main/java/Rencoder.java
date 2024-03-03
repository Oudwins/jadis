// redis enconder
public final class Rencoder {
    // prevent instantiation
    private Rencoder() {
    }

    public static byte[] encode(String s) {
        if (s == null) {
            throw new IllegalArgumentException("null string");
        }
        return s.getBytes(Config.CHARSET);
    }

    public static String decode(byte[] data) {
        return new String(data, Config.CHARSET);
    }
}
