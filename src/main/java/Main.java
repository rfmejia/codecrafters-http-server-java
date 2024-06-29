import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
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
import java.util.zip.GZIPOutputStream;

public class Main {
  static Optional<String> directory = Optional.empty();

  public static void main(final String[] args) throws IOException {
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
          final Job job = new Job(clientSocket);
          executor.execute(job);
        } catch (final IOException ex) {
          System.out.println("IOException while accepting a request: " + ex.getMessage());
        }
      }
    } catch (final Exception ex) {
      System.out.println("Exception during initialization: " + ex.getMessage());
    } finally {
      serverSocket.close();
    }
  }

  // TODO Generalize this
  static Response handle(final Request request) throws IOException {
    final String url = request.url();
    try {
      if (url.equals("/"))
        return Response.OK().withBody(url)
            .withHeader(HttpHeader.ContentType.value(), "text/plain")
            .build();
      else if (url.startsWith("/echo/")) {
        final String input = url.substring("/echo/".length());
        final Response.Builder response = Response.OK();
        final Optional<String[]> encodings = Optional
            .ofNullable(request.headers().get(HttpHeader.AcceptEncoding.value()))
            .map(str -> str.split(","));
        boolean gzipEncodingRequested = encodings.map(encs -> {
          for (final String encoding : encs) {
            if (encoding.trim().equalsIgnoreCase("gzip"))
              return true;
          }
          return false;
        }).orElse(false);

        if (gzipEncodingRequested) {
          final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
          final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteOutputStream);
          final byte[] buffer = input.getBytes(StandardCharsets.UTF_8);
          gzipOutputStream.write(buffer);
          gzipOutputStream.close();
          final byte[] bytes = byteOutputStream.toByteArray();
          byteOutputStream.close();
          response
              .withHeader(HttpHeader.ContentEncoding.value(), "gzip")
              .withHeader(HttpHeader.ContentType.value(), "text/plain")
              .withBody(bytes);
        } else
          response
              .withHeader(HttpHeader.ContentType.value(), "text/plain")
              .withBody(input);
        return response.build();
      } else if (url.startsWith("/user-agent")) {
        final String body = request.headers().get("User-Agent");
        return Response.OK().withBody(body)
            .withHeader(HttpHeader.ContentType.value(), "text/plain")
            .build();
      } else if (url.startsWith("/files/")) {
        if (directory.isEmpty())
          throw new BuilderException("Root directory for static files was not supplied");

        final String filename = url.substring("/files/".length());
        final Path path = Paths.get(directory.get(), filename);

        if (request.method() == HttpMethod.GET) {
          if (!Files.isRegularFile(path))
            return Response.NOT_FOUND().build();

          final byte[] bytes = Files.readAllBytes(path);
          final String content = new String(bytes);
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
    } catch (final BuilderException ex) {
      return Response.INTERNAL_SERVER_ERRROR("Could not build response: " + ex.getMessage());
    }
  }

  static class Job implements Runnable {
    private Socket clientSocket = null;

    public Job(Socket clientSocket) {
      this.clientSocket = clientSocket;
    }

    public void run() {
      BufferedReader in = null;
      DataOutputStream out = null;
      try {
        try {
          in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          out = new DataOutputStream(clientSocket.getOutputStream());

          final Request request = HttpV1_1Protocol.parseRequest(in);
          final Response response = handle(request);
          HttpV1_1Protocol.renderResponse(response, out);
        } catch (final ParseException ex) {
          if (out != null) {
            final Response response = Response.INTERNAL_SERVER_ERRROR("Could not parse request: " + ex.getMessage());
            HttpV1_1Protocol.renderResponse(response, out);
          }
        } finally {
          if (in != null)
            in.close();
          if (out != null)
            out.close();
        }
      } catch (final IOException ex) {
        System.out.print("IOException: ");
        ex.printStackTrace();
      }
    }
  }
}
