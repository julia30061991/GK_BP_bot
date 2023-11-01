package gk_bp_bot.service;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class HtmlParser {
    public static List<String> linkStr = new ArrayList<>();

    public static List<String> parsingUrl(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            Document document = connection.ignoreContentType(true).timeout(3000).get();
            if (document == null) {
                throw new Exception("Подключение отсутствует");
            }
            Elements links = document.select("a.serp-item__title");
            linkStr = new ArrayList<>();
            for (Element link: links) {
                linkStr.add(link.attr("abs:href"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return linkStr;
    }
}