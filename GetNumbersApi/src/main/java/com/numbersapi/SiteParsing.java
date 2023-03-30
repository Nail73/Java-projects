package com.numbersapi;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.LogManager;

public class SiteParsing {
    private static int randomNumber;
    private static Elements elements;

    public static void main(String[] args) throws IOException {
        System.out.println(parseHtml("http://numbersapi.com/#" + randomNumber + "/trivia"));
        frequency();
    }

    protected static String parseHtml(String url) throws IOException {

        randomNumber = (int) (1000 - (Math.random() * 1000));

        LogManager.getLogManager().reset();

        url = "http://numbersapi.com/#" + randomNumber + "/trivia";
        System.out.println("Loading page now------------------------------: " + url);

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(url);
        webClient.waitForBackgroundJavaScript(30 * 1000);
        String pageAsXml = page.asXml();

        Document doc = Jsoup.parse(pageAsXml, url);
        elements = doc.select("#result-temporary-text");

        return "Text: " + elements.text() + "\n";
    }

    protected static void frequency() {
        String text = elements.text().trim();
        String[] textSplit = text.replaceAll("[^0-9a-zA-Zа-яёА-ЯЁ]", "").split("(?!^)");

        Map<String, Integer> letters = new TreeMap<>();

        int count;
        int denominator;
        int divider;
        double averageValue;

        for (String value : textSplit) {
            count = 0;
            for (String s : textSplit) {
                if (value.equals(s)) {
                    count++;
                }
            }
            letters.put(value, count);
        }
        System.out.println("Частоты: ");
        letters.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(System.out::println);

        denominator = letters.values().stream().mapToInt(Integer::intValue).sum();
        divider = letters.size();
        averageValue = (double) denominator / divider;

        System.out.println("\nСреднее значение частоты: " +
                denominator + "/" +
                divider + " = " +
                averageValue);

        for (Map.Entry<String, Integer> symbols : letters.entrySet()) {
            if (symbols.getValue() == Math.round(averageValue)) {
                String str = symbols.getKey();
                StringBuilder sb = new StringBuilder();
                for (char c : str.toCharArray())
                    sb.append((int) c);
                BigInteger ascii = new BigInteger(sb.toString());
                System.out.printf("Symbols is: %s, Value ascii is : %s%n", symbols.getKey(), ascii);
            }
        }
    }
}

