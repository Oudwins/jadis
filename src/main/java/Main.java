import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        int port = 6379;

        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress("localhost", port));
            serverSocket.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                // blocks until channels ready
                if (selector.selectNow() > 0) {
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keys = selectedKeys.iterator();

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        if (key.isAcceptable()) {
                            SocketChannel client = serverSocket.accept();
                            System.out.println("Accepted connection from " + client);
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer buf = ByteBuffer.allocate(1024);
                            int bytesRead = client.read(buf);
                            if (bytesRead == -1) {
                                System.out.println("Something went wrong");
                                key.cancel();
                                client.close();
                                continue;
                            }
                            // to start reading
                            buf.flip();
                            String msg = StandardCharsets.UTF_8.decode(buf).toString();
                            System.out.println("Your message was: " + msg);
                            if (msg.toLowerCase().contains("ping")) {
                                client.write(ByteBuffer.wrap("+PONG\r\n".getBytes(StandardCharsets.UTF_8)));
                            }
                        } else if (key.isWritable()) {
                            System.out.println("writable");
                        }
                        keys.remove();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void handleClient(Socket cs) throws IOException {

        BufferedReader bf = new BufferedReader(new InputStreamReader(cs.getInputStream()));
        OutputStream out = cs.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        String msg;
        while ((msg = bf.readLine()) != null) {
            System.out.println("Client sent: " + msg);
            if (msg.toLowerCase().contains("ping")) {
                writer.println("+PONG\r");
                writer.flush();
            }
        }
    }
}
