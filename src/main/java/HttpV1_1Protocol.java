class HttpV1_1Protocol {
  public static String SPACE = " ";
  public static String HEADER_SEPARATOR = ": ";
  public static String CRLF = "\r\n";
  public static String HTTP_VERSION = "HTTP/1.1";

  public static String render(Response response) {
    StringBuilder sb = new StringBuilder();
    sb.append(HTTP_VERSION).append(SPACE);
    sb.append(response.code()).append(SPACE).append(response.status());
    sb.append(CRLF);
    response.headers().forEach((key, value) ->
      sb.append(key).append(HEADER_SEPARATOR).append(value).append(CRLF));
    sb.append(CRLF);
    response.body().ifPresent(body -> sb.append(body));
    return sb.toString();
  }
}
