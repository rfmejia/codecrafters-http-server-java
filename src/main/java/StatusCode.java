record StatusCode(int code, String message) {
  public static StatusCode OK() { return new StatusCode(200, "OK"); }
  public static StatusCode BadRquest() { return new StatusCode(400, "Bad Request"); }
  public static StatusCode NotFound() { return new StatusCode(404, "Not Found"); }
  public static StatusCode InternalServerError() { return new StatusCode(500, "Internal Server Error"); }
}
