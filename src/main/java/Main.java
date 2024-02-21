import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

//      Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
          serverSocket = new ServerSocket(port);
          serverSocket.setReuseAddress(true);
          // Wait for connection from client.
          clientSocket = serverSocket.accept();
          System.out.println("Client connected " + clientSocket);
          handleClient(clientSocket);
        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        } finally {
          try {
            if (clientSocket != null) {
              clientSocket.close();
            }
          } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
          }
        }
  }
  private static void handleClient(Socket cs) throws IOException {

      BufferedReader bf = new BufferedReader(new InputStreamReader(cs.getInputStream()));
      OutputStream out = cs.getOutputStream();
      PrintWriter writer = new PrintWriter(out, true);
      String msg;
      while ((msg = bf.readLine()) != null) {
          System.out.println("Client sent: " + msg);
          if(msg.contains("ping")) {
              writer.println("+PONG\r");
              writer.flush();
          }
      }
  }
}
