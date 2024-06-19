import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;

public class Main {
  private static String SPACE = " ";
  private static String CRLF = "\r\n";
  private static String HTTP_VERSION = "HTTP/1.1";

  public static void main(String[] args) throws IOException {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    PrintWriter out = null;

    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept(); // Wait for connection from client.
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      String response = handle();
      out.println(response);
      System.out.println("accepted new connection");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      out.close();
    }
  }

  static String handle() {
    StringBuilder sb = new StringBuilder();
    sb.append(HTTP_VERSION);
    sb.append(SPACE);
    sb.append(200);
    sb.append(SPACE);
    sb.append("OK");
    sb.append(CRLF);
    sb.append(CRLF);
    return sb.toString();
  }
}
