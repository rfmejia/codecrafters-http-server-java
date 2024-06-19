import java.util.Optional;

record Response(int code, String status, Optional<String> body) {
  public static Response OK(Optional<String> body) {
    return new Response(200, "OK", body);
  }
  public static Response OK() {
    return OK(Optional.empty());
  }

  public static Response NOT_FOUND(Optional<String> body) {
    return new Response(404, "Not Found", body);
  }
  public static Response NOT_FOUND() {
    return NOT_FOUND(Optional.empty());
  }
}

