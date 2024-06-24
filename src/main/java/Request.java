import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

record Request(HttpMethod method, String url, Map<String, String> headers, Optional<String> body) {
  public static class Builder {
    private HttpMethod method = null;
    private String url = null;
    private Map<String, String> headers = new HashMap<>();
    private String body = null;

    public Builder withMethod(final HttpMethod newMethod) {
      this.method = newMethod;
      return this;
    }

    public Builder withUrl(final String newUrl) {
      this.url = newUrl;
      return this;
    }

    public Builder withHeader(final String key, final String value) {
      if (this.headers == null)
        this.headers = new HashMap<>();
      this.headers.put(key, value);
      return this;
    }

    public Builder withBody(final String newBody) {
      this.body = newBody;
      return this;
    }

    public Request build() throws BuilderException {
      if (this.method == null)
        throw new BuilderException("Method not supplied");
      if (this.url == null)
        throw new BuilderException("URL not supplied");
      return new Request(method, url, headers, Optional.ofNullable(body));
    }
  }
}
