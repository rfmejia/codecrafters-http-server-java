import java.util.Optional;

enum Method {
  GET, POST, PUT, DELETE
}

record Request(Method method, String url) {
  public static Request parse(String raw) {
    if (raw != null) {
      String[] segments = raw.split(HttpV1_1Protocol.CRLF);
      String[] routeParts = segments[0].split(HttpV1_1Protocol.SPACE);
      Method method = null;
      for (Method m : Method.values()) {
        if (routeParts[0] == m.name())
        method = m;
      }
      String url = routeParts[1];
      return new Request(method, url);
    } else return null;
  }
}

