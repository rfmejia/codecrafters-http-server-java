import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/* TODO
 * - Error handling
 * - Route handling
 * - Elegant handling of `Closeable()`
 */

public class Main {
  static Optional<String> directory = Optional.empty();

  public static void main(String[] args) throws IOException {
    // You can use print statements as follows for debugging, they'll be visible
    // when running tests.
    System.out.println("Logs from your program will appear here!");

    for (int i = 0; i < args.length - 1; ++i) {
      if (args[i].equals("--directory")) {
        directory = Optional.of(args[i + 1]);
        break;
      }
    }

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
  static Response handle(Request request) throws IOException {
    String url = request.url();
    try {
      if (url.equals("/"))
        return Response.OK().withBody(url).build();
      else if (url.startsWith("/echo/")) {
        String input = url.substring("/echo/".length());
        Response.Builder response = Response.OK().withBody(input);
        String encoding = request.headers().get(HttpHeader.AcceptEncoding.value());
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
          response.withHeader(HttpHeader.ContentEncoding.value(), "gzip");
        }
        return response.build();
      } else if (url.startsWith("/user-agent")) {
        String body = request.headers().get("User-Agent");
        return Response.OK().withBody(body).build();
      } else if (url.startsWith("/files/")) {
        if (directory.isEmpty())
          throw new BuilderException("Root directory for static files was not supplied");

        String filename = url.substring("/files/".length());
        Path path = Paths.get(directory.get(), filename);

        if (request.method() == HttpMethod.GET) {
          if (!Files.isRegularFile(path))
            return Response.NOT_FOUND().build();

          byte[] bytes = Files.readAllBytes(path);
          String content = new String(bytes);
          return Response.OK().withBody(content)
              .withHeader("Content-Length", Integer.toString(bytes.length))
              .withHeader("Content-Type", "application/octet-stream").build();
        } else if (request.method() == HttpMethod.POST) {
          Files.writeString(path, request.body().orElse(""), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING);
          return Response.CREATED().build();
        } else
          return Response.NOT_FOUND().build();
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
