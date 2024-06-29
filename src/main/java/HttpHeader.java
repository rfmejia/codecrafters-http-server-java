enum HttpHeader {
  AcceptEncoding("Accept-Encoding"),
  ContentEncoding("Content-Encoding"),
  ContentLength("Content-Length"),
  ContentType("Content-Type");

  private String value = null;

  HttpHeader(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }
}

