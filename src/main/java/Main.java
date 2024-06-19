import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Optional;

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
    BufferedReader in = null;
    PrintWriter out = null;

    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      clientSocket = serverSocket.accept(); // Wait for connection from client.

      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      out = new PrintWriter(clientSocket.getOutputStream(), true);

      String request = in.readLine();
      String response = handle(request);
      out.println(response);
      System.out.println("accepted new connection");
    } catch (IOException e) {
      System.out.println("IOException: " + e.getMessage());
    } finally {
      out.close();
    }
  }

  static String handle(String request) {
    Response response = null;
    String url = parseRequest(request);
    if (url.equals("/")) response = Response.OK(Optional.of(url));
    else response = Response.NOT_FOUND();
    return HttpV1_1Protocol.render(response);
  }

  static String parseRequest(String request) {
    String[] segments = request.split(CRLF);
    String[] routeParts = segments[0].split(SPACE);
    return routeParts[1];
  }
}
