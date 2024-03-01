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
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");
        serverWithConcurrentEventLoop();
    }

    private static void serverWithConcurrentEventLoop() {
        Map<String, String> rStore = new HashMap<>();
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
                            ArrayList<Object> req = Protocol.parseRequest(new Rbuf(buf2));
                            System.out.println(req);
                            String cmd = ((String) req.removeFirst()).toLowerCase();
                            String res = "-command not implemented\r\n";
                            System.out.println("CHECKING FOR COMMAND: " + cmd);
                            switch (cmd) {
                                case "ping":
                                    res = Protocol.parseResponseSimple("PONG");
                                    break;
                                case "echo":
                                    if (!req.isEmpty()) {
                                        String rMsg = (String) req.get(0);
                                        res = Protocol.parseResponseString(rMsg);
                                    } else {
                                        res = Protocol.parseResponseString(null);
                                    }
                                    break;
                                case "set":
                                    System.out.println("SET COMMAND FIRED -> " + req.size());
                                    if (req.size() == 2) {
                                        String rKey = (String) req.removeFirst();
                                        String rValue = (String) req.removeFirst();
                                        rStore.put(rKey, rValue);
                                        res = Protocol.parseResponseSimple("OK");
                                    } else {
                                        res = Protocol.parseResponseError("Invalid number of arguments for set command");
                                    }
                                    break;
                                case "get":
                                    if (req.size() == 1) {
                                        String rKey = (String) req.removeFirst();
                                        res = Protocol.parseResponseString(rStore.get(rKey));
                                    } else {
                                        res = Protocol.parseResponseError("Invalid number of arguments for get command");
                                    }
                                    break;
                                default:
                                    System.out.println("GETS TO DEFAULT?");
                                    res = "-command not implemented\r\n";
                            }

                            client.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
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
