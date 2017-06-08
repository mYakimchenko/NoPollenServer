package com.mihanjk.services;

import com.mihanjk.model.Database;
import com.mihanjk.model.Forecast;
import com.mihanjk.model.ForecastNN;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PollenActivityService {
    public static final String tree = "derevev";
    public static final String cereal = "zlakov";
    public static final String weed = "sornykh-trav";
    public static final String spore = "sporonosheniya";

    private static final List<String> types = new ArrayList<>();
    private static final List<ForecastNN> listZlakov = new ArrayList<>();
    private static final List<ForecastNN> listSornykhTrav = new ArrayList<>();
    private static final List<ForecastNN> listDerevev = new ArrayList<>();
    private static final List<ForecastNN> listSporonosheniya = new ArrayList<>();
    private static final Map<String, List<ForecastNN>> forecastNNTemplate = new HashMap<>();

    static {
        types.add(tree);
        types.add(cereal);
        types.add(weed);
        types.add(spore);

        String defaultLevel = "nothing";
        listZlakov.add(new ForecastNN("ОБЩИЙ ФОН", defaultLevel, 0));

        listDerevev.add(new ForecastNN("ОЛЬХА", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ТОПОЛЬ", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ОРЕШНИК", defaultLevel, 0));
        listDerevev.add(new ForecastNN("БЕРЁЗА", defaultLevel, 0));
        listDerevev.add(new ForecastNN("СОСНА", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ЕЛЬ", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ВЯЗ", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ЛИПА", defaultLevel, 0));
        listDerevev.add(new ForecastNN("КЛЁН", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ЯСЕНЬ", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ИВА", defaultLevel, 0));
        listDerevev.add(new ForecastNN("ДУБ", defaultLevel, 0));

        listSornykhTrav.add(new ForecastNN("ПОЛЫНЬ", defaultLevel, 0));
        listSornykhTrav.add(new ForecastNN("КРАПИВА", defaultLevel, 0));
        listSornykhTrav.add(new ForecastNN("ПОДОРОЖНИК", defaultLevel, 0));
        listSornykhTrav.add(new ForecastNN("ЩАВЕЛЬ", defaultLevel, 0));
        listSornykhTrav.add(new ForecastNN("АМБРОЗИЯ", defaultLevel, 0));
        listSornykhTrav.add(new ForecastNN("МАРЕВЫЕ", defaultLevel, 0));

        listSporonosheniya.add(new ForecastNN("КЛАДОСПОРИУМ", defaultLevel, 0));
        listSporonosheniya.add(new ForecastNN("АЛЬТЕРНАРИЯ", defaultLevel, 0));

        forecastNNTemplate.put(cereal, listZlakov);
        forecastNNTemplate.put(tree, listDerevev);
        forecastNNTemplate.put(spore, listSporonosheniya);
        forecastNNTemplate.put(weed, listSornykhTrav);
    }

    private String lastTitleOfPollenForecastNN;
    private EmailService emailService;
    //TODO check which of variable can be final
    private Map<String, List<Forecast>> forecastData;
    private Map<String, Document> documents;
    private Database database;

    @Autowired
    public PollenActivityService(EmailService emailService) {
        this.emailService = emailService;
    }

    public Map<String, List<Forecast>> getForecastData() throws Exception {
        documents = getDocuments();
        forecastData = parseForecastData();

        InputStream pathToFirebaseJson = getClass().getClassLoader().getResourceAsStream("firebase.json");
        if (database == null) {
            database = new Database(pathToFirebaseJson);
        }

        String date = getDateOfForecast(documents.get("derevev"));
        forecastData.forEach((typeOfForecast, forecasts) ->
                database.updateData(typeOfForecast, date, forecasts, Database.MOSCOW_PATH_DATABASE));
        return forecastData;
    }

    private Map<String, Document> getDocuments() throws Exception {
        Map<String, Document> docs = new HashMap<>();
        String prefix = "http://allergotop.com/pyltsevoj-monitoring/prognoz-urovnya-";

        types.forEach(typeOfPollen -> {
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

    public void createTemplate() throws IOException {
        if (haveNewForecast()) {
            //30.05.17
            String notFormatDate = lastTitleOfPollenForecastNN.substring(lastTitleOfPollenForecastNN.length() - 8);
            String date = "2017-" + notFormatDate.substring(3, 5) + "-" + notFormatDate.substring(0, 2);
            forecastNNTemplate.forEach((typeOfForecast, forecasts) ->
                    database.updateData(typeOfForecast, date, forecasts, Database.NN_PATH_DATABASE));
            emailService.sendMailNotification("mihanjk@gmail.com",
                    "Need to update database from nika.nn",
                    "http://nika-nn.ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8");
        }
    }

    private boolean haveNewForecast() throws IOException {
        String link = "http://nika-nn.ru/";
        Document document = Jsoup.connect(link).get();
        String titleOfLastNews = document.getElementsByClass("last-news-list-item_title").get(0)
                .getElementsByTag("a").text();

        if (titleOfLastNews.contains("ПЫЛЬЦЕВОЙ МОНИТОРИНГ") && !titleOfLastNews.equals(lastTitleOfPollenForecastNN)) {
            lastTitleOfPollenForecastNN = titleOfLastNews;
            return true;
        }

        return false;
    }

    public List<ForecastNN> getNNData() {
        return database.getData();
    }
}
