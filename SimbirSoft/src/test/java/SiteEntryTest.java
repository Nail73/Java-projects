import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class SiteEntryTest {

  @Test
  public void entry() {

    Document doc = null;
    try {
      doc = Jsoup.connect("http://en.wikipedia.org/").get();
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert doc != null;
    Elements newsHeadlines = doc.select("#mp-itn b a");
    for (Element headline : newsHeadlines) {
      System.out.println(headline);
    }
  }
}
