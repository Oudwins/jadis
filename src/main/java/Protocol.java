import com.sun.jdi.request.InvalidRequestStateException;

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

    public static final String TERMINATOR = "\r\n";


    private Protocol() {
    }

    public static String parseResponse(Object res) {
        if (res == null) {
            return "$-1" + TERMINATOR;
        }
        if (res instanceof String) {
            return parseResponseString((String) res);
        }
        if (res instanceof Integer) {
            return parseResponseInteger((Integer) res);
        }
        return parseResponseError("Value type not implemented");
    }

    public static String parseResponseString(String res) {
        if (res == null) {
            return "$-1" + TERMINATOR;
        }
        return "$" + res.length() + TERMINATOR + res + TERMINATOR;
    }

    public static String parseResponseError(String e) {
        return "-" + e + TERMINATOR;
    }

    public static String parseResponseSimple(String s) {
        return "+" + s + TERMINATOR;
    }

    public static String parseResponseInteger(Integer i) {
        return ":" + i.toString() + TERMINATOR;
    }

    public static ArrayList<Object> parseRequest(Rbuf buf) {

        final byte b = buf.get();

        switch (b) {
            case ASTERISK_BYTE:
                return processArray(buf);
            default:
                throw new InvalidRequestStateException("Invalid First byte: " + (char) b);
        }

    }


    public static Object process(Rbuf buf) {
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

    public static ArrayList<Object> processArray(Rbuf buf) {
        final int elements = Integer.parseInt(new String(buf.readChunk()));

        ArrayList<Object> list = new ArrayList<>();
        for (int i = 0; i < elements; i++) {
            list.add(process(buf));
        }

        return list;
    }

    public static String processBulkString(Rbuf buf) {
        System.out.println(StandardCharsets.UTF_8.decode(buf.duplicate()).toString());
        System.out.println(StandardCharsets.UTF_8.decode(buf.duplicate()).toString());
        final int chars = Integer.parseInt((new String(buf.readChunk())));
        // terminator
        System.out.println("Attempting to read n chars: " + chars);
        byte[] sbytes = new byte[chars];
        buf.get(sbytes, 0, chars);
        String s = Rencoder.decode(sbytes);

        // terminator
        buf.get();
        buf.get();

        return s;
    }

    public static String processSimpleString(Rbuf buf) {
        // todo need ot differentiate between bulk & simple strigns and errors
        return "testing";
    }

    public static String processError(Rbuf buf) {
        // todo error instance
        return new String(buf.readChunk());
    }

    public static Integer processInt(Rbuf buf) {

        return Integer.parseInt(new String(buf.readChunk()));
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
