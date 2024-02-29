import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        serverWithConcurrentEventLoop();
    }

    private static void serverWithConcurrentEventLoop() {

        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(Protocol.DEFAULT_HOST, Protocol.DEFAULT_PORT));
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
                            ByteBuffer buf = ByteBuffer.allocate(Protocol.DEFAULT_MAX_BULK_STRING_BYTES);
                            int bytesRead = client.read(buf);
                            if (bytesRead == -1) {
                                System.out.println("Something went wrong");
                                key.cancel();
                                client.close();
                                continue;
                            }
                            // to start reading
                            buf.flip();
                            ByteBuffer buf2 = buf.duplicate();
                            String msg = StandardCharsets.UTF_8.decode(buf).toString();
                            System.out.println("Your message was: " + msg);
                            ArrayList<Object> req = Protocol.parseRequest(buf2);
                            String cmd = (String) req.get(0);
                            switch (cmd.toLowerCase()) {
                                case "ping":
                                    client.write(ByteBuffer.wrap("+PONG\r\n".getBytes(StandardCharsets.UTF_8)));
                                case "echo":
                                    String rMsg = (String) req.get(1);
                                    String res = "$" + rMsg.length() + "\r\n" + rMsg + "\r\n";
                                    client.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
                            }
                            System.out.println(req);
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

    private static void serverWithoutConcurrency() {

        try {
            ServerSocket serverSocket = new ServerSocket(Protocol.DEFAULT_PORT);
            System.out.println("TCP Echo Server is running on port " + Protocol.DEFAULT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
