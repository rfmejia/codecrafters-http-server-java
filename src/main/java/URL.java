import java.util.Map;

record URL(String[] path, Map<String, String> params) {
  public static URL parse(final String raw) {
    return null;
  }

}

// /path1/:id/path2?one=1&two=2
// 1. Use `take` a lot

interface Path {

}

record Root() implements Path {}
record Segment() implements Path {}
record ValueSegment(String value) implements Path {}
