import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Rbuf {
    ByteBuffer buf;
    public static final byte[] TERMINATOR = "\r\n".getBytes(Protocol.DEFAULT_CHARSET);

    public Rbuf(ByteBuffer buf) {
        this.buf = buf;
    }


    public byte[] readChunk() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte b1 = buf.get();
        byte b2 = buf.get();
        while (b1 != TERMINATOR[0] && b2 != TERMINATOR[1]) {
            bs.write(b1);
            b1 = b2;
            b2 = buf.get();
        }

        return bs.toByteArray();

    }

    public byte get() {
        return this.buf.get();
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        return this.buf.get(dst, offset, length);
    }

    public ByteBuffer duplicate() {
        return this.buf.duplicate();
    }

}
