class HttpV1_1Protocol {
  private static String SPACE = " ";
  private static String CRLF = "\r\n";
  private static String HTTP_VERSION = "HTTP/1.1";

  public static String render(Response response) {
    StringBuilder sb = new StringBuilder();
    sb.append(HTTP_VERSION).append(SPACE);
    sb.append(response.code()).append(SPACE).append(response.status());
    sb.append(CRLF).append(CRLF);
    response.body().ifPresent(body -> sb.append(body));
    return sb.toString();
  }
}
