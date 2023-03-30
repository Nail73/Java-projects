import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

public class SiteEntry {

  public void entry() {

    try {
      PrintWriter writer = new PrintWriter("data/code.html");
      URL oracle = new URL("https://www.simbirsoft.com/");

      BufferedReader in = new BufferedReader(
          new InputStreamReader(oracle.openStream()));

      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        writer.write(inputLine + "\n");
      }
      writer.flush();
      writer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
