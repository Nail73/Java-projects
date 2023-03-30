import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SiteParsingTest {

  @org.junit.Test
  public void parseFile() {
    StringBuilder builder = new StringBuilder();
    try {
      List<String> lines = Files.readAllLines(Paths.get("data/code.html"));
      lines.forEach(line -> builder.append(line + "\n"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}