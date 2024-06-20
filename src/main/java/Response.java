import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

record Response(int code, String status, Optional<String> body, Map<String, String> headers) {
  public static class Builder {
    private int code = 0;
    private String status = null;
    private String body = null;
    private  Map<String, String> headers = new HashMap<>();

    public Response build() {
      return new Response(code, status, Optional.ofNullable(body), headers);
    }

    public Builder withCode(int newCode) {
      this.code = newCode;
      return this;
    }

    public Builder withStatus(String newStatus) {
      this.status = newStatus;
      return this;
    }

    public Builder withBody(String newBody) {
      this.body = newBody;
      return this.withHeader("Content-Length", "" + this.body.length());
    }

    public Builder withHeader(String key, String value) {
      this.headers.put(key, value);
      return this;
    }
  }

  public static Builder OK() {
    Builder builder = new Builder().withCode(200).withStatus("OK");
    return builder;
  }

  public static Builder NOT_FOUND() {
    Builder builder = new Builder().withCode(404).withStatus("Not Found");
    return builder;
  }
}

