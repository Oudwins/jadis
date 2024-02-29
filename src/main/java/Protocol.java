import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class Protocol {
    int version;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    //    public static final int DEFAULT_TIMEOUT = 2000;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final int DEFAULT_BULK_STRING_BYTES = 512;

    // PROTOCOL CHARACTERS
    public static final byte DOLLAR_BYTE = '$';
    public static final byte ASTERISK_BYTE = '*';
    public static final byte PLUS_BYTE = '+';
    public static final byte MINUS_BYTE = '-';
    public static final byte COLON_BYTE = ':';

    public static final byte[] BYTES_TRUE = toByteArray(1);
    public static final byte[] BYTES_FALSE = toByteArray(0);
    public static final byte[] BYTES_TILDE = Rencoder.encode("~");
    public static final byte[] BYTES_EQUAL = Rencoder.encode("=");
    public static final byte[] BYTES_ASTERISK = Rencoder.encode("*");

    public static final byte[] POSITIVE_INFINITY_BYTES = "+inf".getBytes();
    public static final byte[] NEGATIVE_INFINITY_BYTES = "-inf".getBytes();

    public static final byte[] TERMINATOR = "\r\n".getBytes(DEFAULT_CHARSET);


    public Protocol(int version) {
        this.version = version;
    }


    public static byte[] toByteArray(final double value) {
        if (value == Double.POSITIVE_INFINITY) {
            return POSITIVE_INFINITY_BYTES;
        } else if (value == Double.NEGATIVE_INFINITY) {
            return NEGATIVE_INFINITY_BYTES;
        } else {
            return Rencoder.encode(String.valueOf(value));
        }
    }
}
