import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/* TODO
 * - Error handling
 * - Route handling
 * - Elegant handling of `Closeable()`
 */

public class Main {
  public static void main(String[] args) throws IOException {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Executor executor = null;

    try {
      serverSocket = new ServerSocket(4221);
      // Since the tester restarts your program quite often, setting SO_REUSEADDR
      // ensures that we don't run into 'Address already in use' errors
      serverSocket.setReuseAddress(true);
      executor = Executors.newVirtualThreadPerTaskExecutor();

      while (true) {
        try {
          clientSocket = serverSocket.accept(); // Wait for connection from client.
          BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          Job job = new Job(in, out);
          executor.execute(job);
        } catch (IOException ex) {
          System.out.println("IOException while accepting a request: " + ex.getMessage());
        }
      }
    } catch (IOException ex) {
      System.out.println("IOException during initialization: " + ex.getMessage());
    } finally {
      serverSocket.close();
    }
  }

  // TODO Generalize this
  static Response handle(Request request) {
    String url = request.url();
    try {
      if (url.equals("/"))
        return Response.OK().withBody(url).build();
      else if (url.startsWith("/echo/")) {
        String input = url.substring("/echo/".length());
        return Response.OK().withBody(input).build();
      } else if (url.startsWith("/user-agent")) {
        String body = request.headers().get("User-Agent");
        return Response.OK().withBody(body).build();
      } else
        return Response.NOT_FOUND().build();
    } catch (BuilderException ex) {
      return Response.INTERNAL_SERVER_ERRROR("Could not build response: " + ex.getMessage());
    }
  }

  static class Job implements Runnable {
    private BufferedReader in = null;
    private PrintWriter out = null;

    public Job(BufferedReader in, PrintWriter out) {
      this.in = in;
      this.out = out;
    }

    public void run() {
      try {
        Request request = HttpV1_1Protocol.parseRequest(in);
        Response response = handle(request);
        String rawResponse = HttpV1_1Protocol.renderResponse(response);
        out.print(rawResponse);
      } catch (ParseException ex) {
        if (out != null) {
          Response response = Response.INTERNAL_SERVER_ERRROR("Could not parse request: " + ex.getMessage());
          String rawResponse = HttpV1_1Protocol.renderResponse(response);
          out.print(rawResponse);
        }
      } catch (IOException ex) {
        System.out.println("IOException: " + ex.getMessage());
      } finally {
        out.close();
      }
    }

  }
}
