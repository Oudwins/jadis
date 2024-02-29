import com.sun.jdi.request.InvalidRequestStateException;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public final class Protocol {
    //    int version;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 6379;
    //    public static final int DEFAULT_TIMEOUT = 2000;
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final int DEFAULT_MAX_BULK_STRING_BYTES = 512 * 1000;

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


    private Protocol() {
    }


    public static ArrayList<Object> parseRequest(ByteBuffer buf) {

        final byte b = buf.get();

        switch (b) {
            case ASTERISK_BYTE:
                return processArray(buf);
            default:
                throw new InvalidRequestStateException("Invalid First byte: " + (char) b);
        }

    }


    public static Object process(ByteBuffer buf) {
        final byte b = buf.get();
        switch (b) {
            case PLUS_BYTE:
                return processSimpleString(buf);
            case MINUS_BYTE:
                return processError(buf);
            case COLON_BYTE:
                return processInt(buf);
            case ASTERISK_BYTE:
                return processArray(buf);
            case DOLLAR_BYTE:
                return processBulkString(buf);
            default:
                throw new InvalidRequestStateException("Unidentified byte: " + (char) b);
        }
    }

    public static ArrayList<Object> processArray(ByteBuffer buf) {
        final int elements = Character.getNumericValue(buf.get());
        // TERMINATOR
        buf.get();
        buf.get();

        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < elements; i++) {
            list.add(process(buf));
        }

        return list;
    }

    public static String processBulkString(ByteBuffer buf) {
        final int chars = Character.getNumericValue(buf.get());
        // terminator
        buf.get();
        buf.get();
        byte[] sbytes = new byte[chars];
        buf.get(sbytes, 0, chars);
        String s = Rencoder.decode(sbytes);
        // terminator
        buf.get();
        buf.get();
        return s;
    }

    public static String processSimpleString(ByteBuffer buf) {
        return "testing";
    }

    public static String processError(ByteBuffer buf) {
        return "testing";
    }

    public static Integer processInt(ByteBuffer buf) {
        return 1;
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
