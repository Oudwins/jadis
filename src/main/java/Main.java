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
        Config.setUp(args);
        serverWithConcurrentEventLoop();
    }

    private static void serverWithConcurrentEventLoop() {
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress(Config.HOST, Config.PORT));
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
                        keys.remove();
                        if (key.isAcceptable()) {
                            SocketChannel client = serverSocket.accept();
                            System.out.println("Accepted connection from " + client);
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            ByteBuffer buf = ByteBuffer.allocate(Config.MAX_BULK_STRING_BYTES);
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
                            System.out.println("Your message was: ");
                            System.out.println(msg);
                            System.out.println("------");
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
                                        Memory.set(rKey, rValue);
                                        res = Protocol.parseResponseSimple("OK");
                                    } else if (req.size() == 4) {
                                        String rKey = (String) req.removeFirst();
                                        String rValue = (String) req.removeFirst();
                                        String arg = (String) req.removeFirst();
                                        Integer ms = 0;
                                        Object oMS = req.removeFirst();
                                        if (oMS instanceof String) {
                                            ms = Integer.parseInt((String) oMS);
                                        } else if (oMS instanceof Integer) {
                                            ms = (Integer) oMS;
                                        }
                                        Memory.set(rKey, rValue, ms);
                                        res = Protocol.parseResponseSimple("OK");
                                    } else {
                                        res = Protocol.parseResponseError("Invalid number of arguments for set command");
                                    }
                                    break;
                                case "get":
                                    if (req.size() == 1) {
                                        String rKey = (String) req.removeFirst();
                                        res = Protocol.parseResponse(Memory.get(rKey));
                                    } else {
                                        res = Protocol.parseResponseError("Invalid number of arguments for get command");
                                    }
                                    break;
                                case "config":
                                    if (req.size() == 2) {
                                        String operation = ((String) req.removeFirst()).toLowerCase();
                                        String ckey = (String) req.removeFirst();
                                        switch (operation) {
                                            case "get":
                                                ArrayList<String> l = Config.get(ckey);
                                                res = Protocol.parseResponse(l);
                                                break;
                                            default:
                                                res = Protocol.parseResponseError("Invalid Config Operation");
                                        }
                                    } else {
                                        res = Protocol.parseResponseError("Invalid config argument number");
                                    }
                                default:
                                    res = "-command not implemented\r\n";
                            }

                            client.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static void serverWithoutConcurrency() {

        try {
            ServerSocket serverSocket = new ServerSocket(Config.PORT);
            System.out.println("TCP Echo Server is running on port " + Config.PORT);
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
