package com.mihanjk.services;

import com.mihanjk.model.Database;
import com.mihanjk.model.Forecast;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollenActivityService {
    private static final List<String> types = new ArrayList<>();

    static {
        types.add("derevev");
        types.add("zlakov");
        types.add("sornykh-trav");
        types.add("sporonosheniya");
    }

    //TODO check which of variable can be final
    private Map<String, List<Forecast>> forecastData;
    private Map<String, Document> documents;
    private Database database;

    public Map<String, List<Forecast>> getForecastData() throws Exception {
        documents = getDocuments();
        forecastData = parseForecastData();

        //TODO rename
        InputStream pathToFirebaseJson = getClass().getClassLoader().getResourceAsStream("firebase.json");
        if (database == null) {
            database = new Database(pathToFirebaseJson);
        }

        String date = getDateOfForecast(documents.get("derevev"));
        forecastData.forEach((typeOfForecast, forecasts) -> database.updateData(typeOfForecast, date, forecasts));
        return forecastData;
    }

    Map<String, Document> getDocuments() throws Exception {
        Map<String, Document> docs = new HashMap<>();
        String prefix = "http://allergotop.com/pyltsevoj-monitoring/prognoz-urovnya-";

        types.forEach(typeOfPollen -> {
            // TODO make handling of exception || replace with for
            try {
                docs.put(typeOfPollen, Jsoup.connect(prefix + getSuffixOfURL(typeOfPollen)).get());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });

        return docs;
    }

    private String getSuffixOfURL(String typeOfPollen) {
        return typeOfPollen.equals("sporonosheniya") ? typeOfPollen : "pyleniya-" + typeOfPollen;
    }

    Map<String, List<Forecast>> parseForecastData() {
        // TODO make optimisation
        Map<String, List<Forecast>> data = new HashMap<>();
        documents.forEach((key, value) -> {
            List<Forecast> listOfForecast = new ArrayList<>();
            Element table = value.getElementsByTag("table").get(1);
            Elements rows = table.getElementsByTag("tr");
            for (Element row : rows) {
                Elements columns = row.getElementsByTag("td");
                for (Element column : columns) {
                    Elements divs = column.getElementsByTag("div");
                    if (!divs.isEmpty()) {
                        String name = divs.get(2).text();
                        String currentLevel = divs.get(4).getElementsByTag("img").attr("src");
                        String tomorrowLevel = divs.last().getElementsByTag("img").attr("src");
                        listOfForecast.add(new Forecast(name, getForecastLevel(currentLevel), getForecastLevel(tomorrowLevel)));
                    }
                }
            }
            data.put(key, listOfForecast);
        });
        return data;
    }

    String getForecastLevel(String input) {
        String output;
        switch (input) {
            case "/images/pm/circle-0.png":
                output = "nothing";
                break;
            case "/images/pm/circle-1.png":
                output = "low";
                break;
            case "/images/pm/circle-2.png":
                output = "medium";
                break;
            case "/images/pm/circle-3.png":
                output = "high";
                break;
            case "/images/pm/circle-4.png":
                output = "extra-high";
                break;
            default:
                output = "unexpected value";
                break;
        }
        return output;
    }

    String getDateOfForecast(Document doc) {
        String dataOfForecast = doc.getElementById("dateup").text();
        int beginIndex = dataOfForecast.indexOf(':') + 2;
        return dataOfForecast.substring(beginIndex, beginIndex + 10);
    }

}
