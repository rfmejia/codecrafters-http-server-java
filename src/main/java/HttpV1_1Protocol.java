import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class HttpV1_1Protocol {
  private static String HTTP_VERSION = "HTTP/1.1";
  private static String SPACE = " ";
  private static String HEADER_SEPARATOR = ":";
  private static String CRLF = "\r\n";
  private static char CR = '\r';
  private static char LF = '\n';
  private static int EOF = -1;

  private static String readSection(final Reader reader, final boolean wasCR, final StringBuilder acc)
      throws IOException {
    final int cin = reader.read();
    if (cin == EOF)
      return acc.toString().trim();
    else if (wasCR && cin == LF)
      return acc.toString().trim();
    else if (wasCR && cin != LF)
      return readSection(reader, false, acc.append(CR).append((char) cin));
    else if (cin == CR)
      return readSection(reader, true, acc);
    else
      return readSection(reader, false, acc.append((char) cin));
  }

  private static String readSection(final Reader reader) throws IOException {
    return readSection(reader, false, new StringBuilder());
  }

  // STATE MACHINE
  // start->S1 Read preamble until CRLF
  // S1->S2 Read headers
  // S2->S2a Read key until :
  // S2a->S2 Read value until CRLF
  // S2->S3 CRLF
  // S3->S1 Read body until Content-Length

  private static Request.Builder parsePreamble(final Reader reader, final Request.Builder builder) throws IOException {
    final String preamble = readSection(reader);
    final String[] parts = preamble.split(SPACE);
    for (final HttpMethod method : HttpMethod.values()) {
      if (parts[0].equals(method.name()))
        builder.withMethod(method);
    }
    builder.withUrl(parts[1]);
    return parseHeader(reader, builder);
  }

  private static Request.Builder parseHeader(final Reader reader, final Request.Builder builder, final int length)
      throws IOException {
    final String section = readSection(reader);
    if (section.length() == 0)
      return parseBody(reader, builder, length);
    else {
      final String[] parts = section.split(HEADER_SEPARATOR);
      final String key = parts[0].trim();
      final String value = parts[1].trim();
      builder.withHeader(key, value);
      if (key.equalsIgnoreCase(HttpHeader.ContentLength.value()))
        return parseHeader(reader, builder, Integer.parseInt(value));
      else
        return parseHeader(reader, builder, length);
    }
  }

  private static Request.Builder parseHeader(final Reader reader, final Request.Builder builder) throws IOException {
    return parseHeader(reader, builder, 0);
  }

  private static Request.Builder parseBody(final Reader reader, final Request.Builder builder, final int length)
      throws IOException {
    if (length > 0) {
      final char[] c = new char[length];
      reader.read(c, 0, length);
      builder.withBody(new String(c));
    }
    return builder;
  }

  public static Request parseRequest(final Reader reader) throws ParseException, IOException {
    try {
      return parsePreamble(reader, new Request.Builder()).build();
    } catch (final BuilderException ex) {
      throw new ParseException("Could not build request: " + ex.getMessage());
    } catch (final ArrayIndexOutOfBoundsException ex) {
      ex.printStackTrace();
      throw new ParseException("Malformed HTTP request");
    }
  }

  static byte[] asUTF8(String str) {
    return str.getBytes(StandardCharsets.UTF_8);
  }

  public static void renderResponse(final Response response, final OutputStream out) throws IOException {
    out.write(asUTF8(HTTP_VERSION + SPACE + response.statusCode().code()));
    out.write(asUTF8(SPACE + response.statusCode().message() + CRLF));
    if (response.headers() != null) {
      for (Map.Entry<String, String> entry : response.headers().entrySet()) {
        String header = entry.getKey() + HEADER_SEPARATOR + SPACE + entry.getValue() + CRLF;
        out.write(asUTF8(header));
      }
    }
    out.write(asUTF8(CRLF));
    if (response.body().isPresent()) {
      out.write(response.body().get());
    }
  }
}
