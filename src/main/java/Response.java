import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

record Response(StatusCode statusCode, Optional<String> body, Map<String, String> headers) {
  public static class Builder {
    private StatusCode statusCode = null;
    private String body = null;
    private final Map<String, String> headers = new HashMap<>();

    public Response build() throws BuilderException {
      if (statusCode == null)
        throw new BuilderException("Response code not supplied");
      return new Response(statusCode, Optional.ofNullable(body), headers);
    }

    public Builder(final StatusCode code) {
      this.statusCode = code;
    }

    public Builder withBody(final String newBody) {
      this.body = newBody;
      return this
          .withHeader(HttpHeader.ContentType.value(), "text/plain")
          .withHeader(HttpHeader.ContentLength.value(), "" + this.body.length());
    }

    public Builder withHeader(final String key, final String value) {
      this.headers.put(key, value);
      return this;
    }
  }

  public static Builder OK() {
    return new Builder(StatusCode.OK());
  }

  public static Builder NOT_FOUND() {
    return new Builder(StatusCode.NotFound());
  }

  public static Response INTERNAL_SERVER_ERRROR(String body) {
    Map<String, String> headers = null;
    if (body != null) {
      headers = new HashMap<>();
      headers.put(HttpHeader.ContentType.value(), "text/plain");
      headers.put(HttpHeader.ContentLength.value(), "" + body.length());
    }
    return new Response(StatusCode.InternalServerError(), Optional.of(body), headers);
  }
}
