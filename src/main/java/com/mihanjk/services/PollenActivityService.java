package com.mihanjk.services;

import com.mihanjk.model.Database;
import com.mihanjk.model.Forecast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollenActivityService {
    private static final Map<String, String> types = new HashMap<>();
    private static Map<String, Document> docs = new HashMap<>();

    static {
        types.put("tree", "derevev");
        types.put("cereal", "zlakov");
        types.put("weed", "sornykh-trav");
        types.put("spore", "sporonosheniya");
    }

    //TOdo add support for multiple regions
    //TODO check which of variable can be final
    private Map<String, List<Forecast>> data = new HashMap<>();

    public Map<String, List<Forecast>> parseData(String type) throws Exception {
        //Todo data always adding new request into previous JSON
        //Todo add exception for unexpected type of request
        if (!(types.containsKey(type) || type.equals("all"))) {
            throw new Exception("Unexpected type of request. Available parameters of type: tree, cereal, weed, spore");
        }

        try {
            //TODO make refactoring
            String prefix = "http://allergotop.com/pyltsevoj-monitoring/prognoz-urovnya-";
            if (type.equals("all")) {
                types.forEach((alias, typeOfSite) -> {
                    // TODO make handling of exception || replace with for
                    try {
                        if (alias.equals("spore")) {
                            docs.put(alias, Jsoup.connect(prefix + typeOfSite).get());
                        } else {
                            docs.put(alias, Jsoup.connect(prefix + "pyleniya-" + typeOfSite).get());
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            } else {
                // TODO why i don't need handling IOException here?
                if (type.equals("spore")) {
                    docs.put(type, Jsoup.connect(prefix + types.get(type)).get());
                } else {
                    docs.put(type, Jsoup.connect(prefix + "pyleniya-" + types.get(type)).get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO make optimisation
        docs.forEach((key, value) -> {
            List<Forecast> listOfForecast = new ArrayList<>();
            Element table = value.getElementsByTag("table").last();
            Elements rows = table.getElementsByTag("tr");
            for (Element row : rows) {
                Elements columns = row.getElementsByTag("td");
                for (Element column : columns) {
                    Elements divs = column.getElementsByTag("div");
                    if (!divs.isEmpty()) {
                        String name = divs.get(2).text();
                        String currentLevel = divs.get(4).getElementsByTag("img").attr("src");
                        String tomorrowLevel = divs.last().getElementsByTag("img").attr("src");
                        listOfForecast.add(new Forecast(name, currentLevel, tomorrowLevel));
                    }
                }
            }
            data.put(key, listOfForecast);
        });

        //TODO refactoring fucking ugly code
        String date = getDateOfForecast(docs.values().stream().findFirst().get());
        URI pathToFirebaseJson = this.getClass().getClassLoader().getResource("firebase.json").toURI();
        Database database = new Database(pathToFirebaseJson);

        data.forEach((s, forecasts) -> database.updateData(s, date, forecasts));

        return data;
    }

    String getDateOfForecast(Document doc) {
        String dataOfForecast = doc.getElementById("dateup").text();
        int beginIndex = dataOfForecast.indexOf(':') + 2;
        return dataOfForecast.substring(beginIndex, beginIndex + 10);
    }
}
