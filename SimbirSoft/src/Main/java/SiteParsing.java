import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;

public class SiteParsing {

  private static Logger log;

  public static void main(String[] args) throws IOException {

    log = LogManager.getRootLogger();
    log.info("Статус работы ");

    SiteEntry siteEntry = new SiteEntry();
    siteEntry.entry();

//
    String htmlFile = parseFile("data/code.html");

    Document doc = Jsoup.parse(htmlFile);
    Elements elements = doc.select("div");
    String text = elements.text().trim().toUpperCase();
    String[] textSplit = text.replaceAll("[^a-zA-Zа-яёА-ЯЁ]", " ").split(" ");

    Map<String, Integer> mp = new TreeMap<>();

    int count = 0;
    for (String value : textSplit) {
      count = 0;
      for (String s : textSplit) {
        if (value.equals(s)) {
          count++;
        }
      }
      mp.put(value, count);
    }
    log.error("Статус ошибки ");
    mp.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .forEach(System.out::println);
  }

  //
  public static String parseFile(String path) {

    StringBuilder builder = new StringBuilder();
    try {
      List<String> lines = Files.readAllLines(Paths.get(path));
      lines.forEach(line -> builder.append(line + "\n"));
    } catch (Exception ex) {
      log.error("Ошибка вида: ", ex);
    }
    return builder.toString();
  }
}









