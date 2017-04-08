package com.mihanjk.services;

import com.mihanjk.model.Forecast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PollenActivityService {
    private static List<Forecast> data = new ArrayList<>();
    //Todo data always adding new request into JSON

    public List<Forecast> getData(String type) throws Exception {
        Document doc = null;

        if (!(type.equals("derevo") || type.equals("zlak") || type.equals("trava") || type.equals("mushrooms"))) {
            System.err.println("Unexpected type of request");
        }

        try {
            doc = Jsoup.connect("http://allergotop.com/fon-" + type + ".php?region=1").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element table = doc.select("table").first();
        Elements rows = table.select("tr");
        for (int i = 1; i < rows.size(); ++i) {
            Elements cols = rows.get(i).select("td");
            String name = cols.get(0).select("p").text();
            String currentLevel = cols.get(1).select("img").attr("src");
            String tomorrowLevel = cols.get(2).select("img").attr("src");
            data.add(new Forecast(name, currentLevel, tomorrowLevel));
        }
        return data;
    }
}
